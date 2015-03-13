/*
 *  Copyright 2015 n3integration
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.n3integration.lookup.geoip

import java.io._
import java.util.zip._
import java.nio.file._

import sys.process._
import scala.io.Source
import scala.concurrent._
import ExecutionContext.Implicits.global

/**
 * Various IO utilities
 */
object IO {

  /**
   * Downloads a zip archive and extracts it's contents to the provided path
   *
   * @param from
   *          the source URL
   * @param to
   *          the target path
   */
  def downloadAndExtract(from: String, to: String) = future {
    val tmpFile = """/tmp/${from.substring(from.lastIndexOf("/") + 1)}"""
    IO.download(from, tmpFile)
    IO.unzip(tmpFile, to)
  }

  /**
   * Downloads a file from a supported URL (e.g. http[s])
   *
   * @param from
   *            the source URL
   * @param to
   *           the target filename
   */
  def download(from: String, to: String) = {
    new java.net.URL(from) #> new File(to) !
  }

  /**
   * Unzips the provided file to the specified path
   *
   * @param file
   *            the source zip archive
   * @param to
   *           the target path
   */
  def unzip(file: String, to: String) = {
    val zipFile = new ZipFile(file)

    try {
      ZipFileIterator(zipFile).foreach { entry =>
        val instream = zipFile.getInputStream(entry)
        try {
          Files.copy(instream, new File(s"${to}/${entry.getName()}").toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
        finally {
          instream.close()
        }
      }
    }
    finally {
      zipFile.close()
    }
  }

  case class ZipFileIterator(zip: ZipFile) extends Iterator[ZipEntry] {
    val entries = zip.entries()

    override def hasNext() = entries.hasMoreElements()
    override def next() = entries.nextElement()
  }
}
