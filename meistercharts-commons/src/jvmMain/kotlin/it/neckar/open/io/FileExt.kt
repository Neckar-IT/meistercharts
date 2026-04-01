/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.io

import java.io.File
import java.nio.charset.Charset


/** Writes into a file by first creating a temp file and then renaming the temp file to replace the actual target file.
 * Should the program crash while executing, the tmp file will remain next to the target file.*/
fun File.writeTextWithRename(text: String, charset: Charset = Charsets.UTF_8) {
  val tmpFile: File = this.createTmpFile()
  tmpFile.writeText(text, charset)
  tmpFile.renameTo(this)
}

/** Creates a corresponding tmp file for this file. The tmp file has an appendix consisting of a TMP suffix (.tmp) and the current nanoTime. */
fun File.createTmpFile(): File {
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

/**
 * Writes the given ByteArray into the file by first creating a temp file and then renaming the temp file to replace the actual target file.
 */
fun File.writeBytesWithRename(array: ByteArray) {
  val tmpFile: File = this.createTmpFile()
  tmpFile.writeBytes(array)
  tmpFile.renameTo(this)
}


const val SUFFIX_TMP: String = ".tmp"
