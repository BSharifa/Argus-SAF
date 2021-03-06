/*
 * Copyright (c) 2016. Fengguo Wei and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Detailed contributors are listed in the CONTRIBUTOR.md
 */

package org.argus.amandroid.concurrent

import java.util.concurrent.TimeoutException

import akka.actor._
import org.argus.amandroid.concurrent.util.GlobalUtil
import org.argus.amandroid.core.Apk
import org.argus.amandroid.core.appInfo.AppInfoCollector
import org.argus.amandroid.serialization.stage.Staging
import org.argus.jawa.core.util.FutureUtil
import org.argus.jawa.core.{MsgLevel, PrintReporter}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.{global => sc}

class ApkInfoCollectActor extends Actor with ActorLogging {
  def receive: Receive = {
    case acd: ApkInfoCollectData =>
      sender ! collectInfo(acd)
  }
  
  private def collectInfo(acdata: ApkInfoCollectData): ApkInfoCollectResult = {
    log.info("Start collect info for " + acdata.fileUri)
    val srcs = acdata.srcFolders
    val outApkUri = acdata.outApkUri
    val reporter = new PrintReporter(MsgLevel.ERROR)
    val global = GlobalUtil.buildGlobal(acdata.fileUri, reporter, outApkUri, srcs)
    val apk = new Apk(acdata.fileUri, outApkUri, srcs)
    val (f, cancel) = FutureUtil.interruptableFuture[ApkInfoCollectResult] { () =>
      try {
        AppInfoCollector.collectInfo(apk, global, outApkUri)
        ApkInfoCollectSuccResult(apk, acdata.outApkUri, acdata.srcFolders)
      } catch {
        case e: Exception =>
          ApkInfoCollectFailResult(acdata.fileUri, e)
      }
    }
    val res =
      try {
        val res = Await.result(f, acdata.timeout)
        try {
          Staging.stageApk(apk, outApkUri)
        } catch {
          case e: Exception =>
            PointsToAnalysisFailResult(apk.nameUri, e)
        }
        res
      } catch {
        case te: TimeoutException =>
          cancel()
          log.warning("Collect apk info timeout for " + acdata.fileUri)
          ApkInfoCollectFailResult(acdata.fileUri, te)
      }
    res
  }
}
