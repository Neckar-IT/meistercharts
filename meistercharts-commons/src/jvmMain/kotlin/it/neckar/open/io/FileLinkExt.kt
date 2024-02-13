/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */
package it.neckar.open.io

import it.neckar.open.file.requireIsFile
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.Random

/**
 * Returns true if this file is a symlink to the provided target
 */
fun File.isSymLinkTo(targetFile: File): Boolean {
  if (!exists()) {
    return false
  }
  val canonicalPath = canonicalPath
  val absolutePath = absolutePath
  return absolutePath != canonicalPath
}

/**
 * Checks whether a given file is a symbolic link.
 */
fun File.isSymbolicLink(): Boolean {
  this.requireIsFile()

  return absoluteFile != canonicalFile
}

/**
 * Returns whether the given file is a link
 */
@Deprecated("Inline!", ReplaceWith("this.isSymbolicLink()"))
fun File.isLink(): Boolean {
  return this.isSymbolicLink()
}

/**
 * Creates a link
 */
@Deprecated("Use createLink instead", ReplaceWith("createLink(linkTarget, linkFile, linkType)"))
fun createLink(linkTarget: File, linkFile: File, symbolic: Boolean): Boolean {
  return createLink(linkTarget, linkFile, if (symbolic) LinkType.SYMBOLIC else LinkType.HARD)
}

/**
 * Creates a symbolik link
 *
 * @param linkTarget the link source
 * @param linkFile   the link file
 * @return whether the link has been created
 *
 * @throws IOException if any.
 */
fun createSymbolicLink(linkTarget: File, linkFile: File): Boolean {
  return createLink(linkTarget, linkFile, true)
}

/**
 * Creates a hard link
 *
 * @param linkTarget the link source
 * @param linkFile   the link file
 * @return whether the link has been created
 *
 * @throws IOException if any.
 */
fun createHardLink(linkTarget: File, linkFile: File): Boolean {
  return createLink(linkTarget, linkFile, false)
}

/**
 * Creates a link.
 * Returns true if the link has been created, false if the link (with the same link source) still exists.
 *
 * @param linkTarget the link source
 * @param linkFile   the link file
 * @param symbolic   whether to create a symbolic link
 * @return whether the link has been created (returns false if the link still existed)
 *
 * @throws IOException if something went wrong
 */
fun createLink(linkTarget: File, linkFile: File, linkType: LinkType): Boolean {
  if (linkFile.exists()) {
    if (linkType == LinkType.HARD) {
      //Maybe the hard link still exists - we just don't know, so throw an exception
      throw IOException("link target already exists: " + linkFile.absolutePath)
    }

    if (linkTarget.canonicalFile == linkFile.canonicalFile) {
      //still exists - that is ok, since it points to the same directory
      return false
    } else {
      //Other target
      throw AlreadyExistsWithOtherTargetException(linkTarget, linkFile)
    }
  }

  val args: MutableList<String> = ArrayList()
  args.add("ln")
  if (linkType == LinkType.SYMBOLIC) {
    args.add("-s")
  }
  args.add(linkTarget.path)
  args.add(linkFile.absolutePath)

  val builder = ProcessBuilder(args)
  val process = builder.start()
  try {
    val result = process.waitFor()
    if (result != 0) {
      throw IOException("Creation of link failed: " + IOUtils.toString(process.errorStream, Charset.defaultCharset()))
    }
  } catch (e: InterruptedException) {
    throw RuntimeException(e)
  }
  return true
}

/**
 * Creates a temporary file
 *
 * @param prefix    the prefix
 * @param suffix    the suffix
 * @param parentDir the parent dir
 * @return the created file
 */
fun createTempFile(prefix: String, suffix: String, parentDir: File?): File {
  val rand = Random()
  val parent = if (parentDir == null) System.getProperty("java.io.tmpdir") else parentDir.path
  val fmt = DecimalFormat("#####")
  var result: File
  do {
    result = File(parent, prefix + fmt.format(Math.abs(rand.nextInt()).toLong()) + suffix)
  } while (result.exists())
  return result
}


class AlreadyExistsWithOtherTargetException(linkTarget: File, linkFile: File) : Exception("A link still exists at <" + linkFile.absolutePath + "> but with different target: <" + linkTarget.canonicalPath + "> exected <" + linkFile.canonicalPath + ">")
