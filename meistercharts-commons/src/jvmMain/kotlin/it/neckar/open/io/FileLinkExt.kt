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
  //Must be a symlink at all, and its resolved target must be the provided file.
  return absolutePath != canonicalPath && canonicalFile == targetFile.canonicalFile
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
@IgnorableReturnValue
@Deprecated("Use createLink instead", ReplaceWith("createLink(linkTarget, linkFile, linkType)"))
fun createLink(linkTarget: File, linkFile: File, symbolic: Boolean): Boolean {
  return createLink(linkTarget, linkFile, if (symbolic) LinkType.SYMBOLIC else LinkType.HARD)
}

/**
 * Creates a symbolik link
 *
 * @param linkTarget the link source
 * @param linkFile   the link file
 * @return `true` when a new link was created, `false` when the link already existed. Callers
 * that only need the side effect (link exists afterwards) may discard the return.
 *
 * @throws IOException if any.
 */
@IgnorableReturnValue
fun createSymbolicLink(linkTarget: File, linkFile: File): Boolean {
  return createLink(linkTarget, linkFile, true)
}

/**
 * Creates a hard link
 *
 * @param linkTarget the link source
 * @param linkFile   the link file
 * @return `true` when a new link was created, `false` when the link already existed. Callers
 * that only need the side effect (link exists afterwards) may discard the return.
 *
 * @throws IOException if any.
 */
@IgnorableReturnValue
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
@IgnorableReturnValue
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
    //Restore the interrupt flag - swallowing it hides the cancellation from every caller up the stack.
    Thread.currentThread().interrupt()
    throw IOException("Interrupted while waiting for the link creation of [${linkFile.absolutePath}]", e)
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
