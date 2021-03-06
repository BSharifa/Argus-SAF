/*
 * Copyright (c) 2016. Fengguo Wei and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Detailed contributors are listed in the CONTRIBUTOR.md
 */

package org.argus.amandroid.core

import java.io.{File, FileInputStream, InputStream}

import org.argus.jawa.core.util.MyFileUtil
import org.ini4j.Wini
import org.sireum.util.FileUtil

/**
  * @author <a href="mailto:fgwei521@gmail.com">Fengguo Wei</a>
  */
class AmandroidSettings(amandroid_home: String, iniPathOpt: Option[String]) {
  private val amandroid_home_url = FileUtil.toUri(amandroid_home)
  private def defaultLibFiles =
    amandroid_home + "/androidSdk/android-21/android.jar" + java.io.File.pathSeparator +
    amandroid_home + "/androidSdk/support/v4/android-support-v4.jar" + java.io.File.pathSeparator +
    amandroid_home + "/androidSdk/support/v13/android-support-v13.jar" + java.io.File.pathSeparator +
    amandroid_home + "/androidSdk/support/v7/android-support-v7-appcompat.jar"
  private val iniUrl = {
    iniPathOpt match {
      case Some(path) => FileUtil.toUri(path)
      case None => MyFileUtil.appendFileName(amandroid_home_url, "config.ini")
    }
  }
  private val ini = new Wini(FileUtil.toFile(iniUrl))
  def timeout: Int = Option(ini.get("analysis", "timeout", classOf[Int])).getOrElse(10)
  def dependence_dir: Option[String] = Option(ini.get("general", "dependence_dir", classOf[String]))
  def debug: Boolean = ini.get("general", "debug", classOf[Boolean])
  def lib_files: String = Option(ini.get("general", "lib_files", classOf[String])).getOrElse(defaultLibFiles)
  def actor_conf_file: InputStream = Option(ini.get("concurrent", "actor_conf_file", classOf[String])) match {
    case Some(path) => new FileInputStream(path)
    case None => getClass.getResourceAsStream("/application.conf")
  }
  def static_init: Boolean = ini.get("analysis", "static_init", classOf[Boolean])
  def parallel: Boolean = ini.get("analysis", "parallel", classOf[Boolean])
  def k_context: Int = ini.get("analysis", "k_context", classOf[Int])
  def sas_file: String = Option(ini.get("analysis", "sas_file", classOf[String])).getOrElse(amandroid_home + File.separator + "taintAnalysis" + File.separator + "sourceAndSinks" + File.separator + "TaintSourcesAndSinks.txt")
}
