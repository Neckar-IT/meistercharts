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
import java.io.FilterOutputStream
import java.io.IOException
import java.nio.file.Files

/**
 * Writes to a temporary file and moves to the target file name on [close]
 */
class FileOutputStreamWithMove(val file: File) : FilterOutputStream(null) {
  /**
   * Whether the stream has been closed already
   */
  private var closed: Boolean = false

  /**
   * The tmp file that is written first
   */
  val tmpFile: File = File(file.parent, file.name + SUFFIX_TMP + "_" + System.nanoTime()).also {
    it.deleteOnExit()

    this.out = it.outputStream().buffered()
  }

  override fun close() {
    super.close()

    if (closed) {
      return
    }

    //Only move the file if it exists
    if (tmpFile.exists()) {
      //delete the original file first - overwrite mode
      if (file.exists()) {
        if (!file.delete()) {
          throw IOException("Failed to delete existing file before move: ${file.absolutePath}")
        }
      }

      Files.move(tmpFile.toPath(), file.toPath())
    }
    closed = true
  }

  companion object {
    /**
     * The suffix for the tmp file
     */
    const val SUFFIX_TMP: String = ".tmp"
  }
}

/**
 * Creates a new file input stream that first writes to a tmp file and moves the file on close
 */
fun File.outputStreamWithMove(): FileOutputStreamWithMove {
  return FileOutputStreamWithMove(this)
}
