/*
 * Copyright (c) 2016. Fengguo Wei and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Detailed contributors are listed in the CONTRIBUTOR.md
 */
package org.argus.amandroid.core.util

import org.sireum.util._
import java.io.PrintWriter

import org.argus.amandroid.core.dedex.PilarDeDex
import org.argus.amandroid.core.parser.ManifestParser
import org.argus.jawa.core.util.MyFileUtil

/**
 * @author <a href="mailto:fgwei521@gmail.com">Fengguo Wei</a>
 */
object FixResources {
  def fix(decFolder: FileResourceUri, dedex: PilarDeDex) = {
    val allxmls = FileUtil.listFiles(decFolder, ".xml", recursive = true)
    allxmls foreach {
      xml =>
        if(xml.endsWith("AndroidManifest.xml") && !xml.endsWith("original/AndroidManifest.xml")) {
          if(dedex.haveRenamedElements) {
            var filestr = MyFileUtil.readFileContent(xml)
//            val pkgMap = dedex.getPkgNameMapping
//            val recMap = dedex.getRecordNameMapping
            val (pkg, recs) = ManifestParser.loadPackageAndComponentNames(xml)
            val newpkg = dedex.mapPackage(pkg)
            filestr = filestr.replaceAll("\"" + pkg + "\"", "\"" + newpkg + "\"")
//            val classparts: MList[String] = mlistEmpty
            recs.foreach {
              case (origstr, comclass) =>
                val newclass = dedex.mapRecord(comclass)
                filestr = filestr.replaceAll("\"" + origstr + "\"", "\"" + newclass + "\"")
            }
            val pw = new PrintWriter(FileUtil.toFile(xml))
            pw.write(filestr)
            pw.flush()
            pw.close()
          }
        } else if(xml.contains("/res/layout/")) {
          
        }
    }
  }
}
