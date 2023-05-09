/**
 * Copyright (C) cedarsoft GmbH.

 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at

 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)

 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.

 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).

 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.

 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */
package it.neckar.open.io

import java.io.File
import java.io.IOException
import java.nio.charset.Charset

/**
 */
object FileUtils {
  /**
   * Throws an IOException if the given file is not a directory
   */
  @JvmStatic
  @Throws(IOException::class)
  fun ensureDirectoryExists(directory: File) {
    if (!directory.exists()) {
      throw IOException("Does not exist <" + directory.absolutePath + ">")
    }
    if (!directory.isDirectory) {
      throw IOException("Directory not found <" + directory.absolutePath + ">")
    }
  }
}

/**
 * Creates the directory if it does not exist
 */
fun File.createDirectoryIfNotExisting(): File {
  FileUtils.ensureDirectoryExists(this)
  return this
}

/** Writes into a file by first creating a temp file and then renaming the temp file to replace the actual target file.
 * Should the program crash while executing, the tmp file will remain next to the target file.*/
fun File.writeTextWithRename(text: String, charset: Charset = Charsets.UTF_8) {
  val tmpFile: File = this.createTmpFile()
  tmpFile.writeText(text,charset)
  tmpFile.renameTo(this)
}

/** Creates a corresponding tmp file for this file. The tmp file has an appendix consisting of a TMP suffix (.tmp) and the current nanoTime. */
fun File.createTmpFile():File {
  return File(this.parent, this.name + SUFFIX_TMP + "_" + System.nanoTime())
}

/** Creates corresponding temporary backup directories for this directory and
 * then replaces the old directory with the new one, returns the converted File.
 * This file will be replaced by the source Directory.
 * Source directory should be on the same partition as this file.
 * The source directory will be replaced when this method is done.*/
fun File.replaceDirWithRename(sourceDirectory: File) {
  val backupDirectory = File(this.parentFile, this.name + ".old")
  // Replace the old storage directory with the new, converted storage directory
  // by first renaming storageBaseDirToConvert to a backupDirectory and then renaming newDirectory to storageBaseDirToConvert
  this.renameTo(backupDirectory)
  sourceDirectory.renameTo(this)

  // delete backup and temp rename Directory
  backupDirectory.deleteRecursively()
}


const val SUFFIX_TMP: String = ".tmp"
