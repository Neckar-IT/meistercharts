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
package it.neckar.open.file

import it.neckar.open.kotlin.lang.encodeForFileName
import java.io.File

/**
 * File extensions
 */


/**
 * Returns a new file that replaces a leading "~/" with the user home.
 *
 * Returns this, if the path does *not* start with "~/"
 */
fun File.replaceLeadingTilde(): File {
  if (path.startsWith("~/")) {
    return File(path.replaceFirst("~/", "${System.getProperty("user.home")}/"))
  }

  return this
}

/**
 * Creates a tree representation of the file system
 */
fun File.tree(showOwnName: Boolean = false): String = buildString {
  return treeRecursively(showDotInsteadOfCurrentName = showOwnName.not())
}

private fun File.treeRecursively(prefix: String = "", continuation: String = "", showDotInsteadOfCurrentName: Boolean = true): String = buildString {
  if (showDotInsteadOfCurrentName) {
    append(".\n")
  } else {
    append("$prefix${this@treeRecursively.name}\n")
  }

  if (this@treeRecursively.isDirectory) {
    val files = this@treeRecursively.listFiles()
    files?.let {
      it.sort()
      it.indices.forEach { i ->
        val isLast = i == it.size - 1
        val newPrefix = continuation + if (isLast) "└── " else "├── "
        val newContinuation = continuation + if (isLast) "    " else "│   "
        append(it[i].treeRecursively(newPrefix, newContinuation, false))
      }
    }
  }
}

/**
 * Throws an exception if this file is not a file (e.g. does not exist or is a directory).
 * Returns [this] to support fluent usage; the return value may be safely ignored.
 */
@IgnorableReturnValue
fun File.requireIsFile(messageProvider: (File) -> String = { "File <${this.absolutePath}> is not a file" }): File {
  require(this.isFile) { messageProvider(this) }
  return this
}

/**
 * Throws an exception if this file is not a directory.
 * Returns [this] to support fluent usage; the return value may be safely ignored.
 */
@IgnorableReturnValue
fun File.requireIsDirectory(messageProvider: (File) -> String = { "File <${this.absolutePath}> is not a directory" }): File {
  require(this.isDirectory) { messageProvider(this) }
  return this
}

/**
 * Creates a new child file
 */
fun File.file(path: String): File {
  return File(this, path)
}


/**
 * Helper method to improve visibility of the code completion
 */
@Suppress("NOTHING_TO_INLINE")
inline fun String.toSafeFileName(): String {
  return encodeForFileName()
}
