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

import java.io.File

/**
 * Resolves the relative path
 *
 */
object RelativePathFinder {
  private const val BACKSLASH = '\\'
  private const val SLASH = '/'

  /**
   * Calculates the relative path
   *
   * @param target    the target path
   * @param base      the base (a directory)
   * @param separator the separator
   * @return the relative path pointing to the target (from the base)
   */
  @JvmStatic
  fun getRelativePath(target: String, base: String, separator: String): String {
    //
    // remove trailing file separator
    //
    var canonicalBase = base
    if (base[base.length - 1] == SLASH || base[base.length - 1] == BACKSLASH) {
      canonicalBase = base.substring(0, base.length - 1)
    }

    //
    // get canonical name of target and remove trailing separator
    //
    var canonicalTarget = target
    if (canonicalTarget[canonicalTarget.length - 1] == SLASH || canonicalTarget[canonicalTarget.length - 1] == BACKSLASH) {
      canonicalTarget = canonicalTarget.substring(0, canonicalTarget.length - 1)
    }
    if (canonicalTarget == canonicalBase) {
      return "."
    }

    //
    // see if the prefixes are the same
    //
    if (canonicalBase.substring(0, 2) == "\\\\") {
      //
      // UNC file name, if target file doesn't also start with same
      // server name, don't go there
      val endPrefix = canonicalBase.indexOf(BACKSLASH, 2)
      val prefix1 = canonicalBase.substring(0, endPrefix)
      val prefix2 = canonicalTarget.substring(0, endPrefix)
      if (prefix1 != prefix2) {
        return canonicalTarget
      }
    } else {
      if (canonicalBase.substring(1, 3) == ":\\") {
        val endPrefix = 2
        val prefix1 = canonicalBase.substring(0, endPrefix)
        val prefix2 = canonicalTarget.substring(0, endPrefix)
        if (prefix1 != prefix2) {
          return canonicalTarget
        }
      } else {
        if (canonicalBase[0] == SLASH) {
          if (canonicalTarget[0] != SLASH) {
            return canonicalTarget
          }
        }
      }
    }

    // char separator = File.separatorChar;
    var minLength = canonicalBase.length
    if (canonicalTarget.length < minLength) {
      minLength = canonicalTarget.length
    }
    var firstDifference = minLength + 1

    //
    // walk to the shorter of the two paths
    // finding the last separator they have in common
    var lastSeparator = -1
    for (i in 0 until minLength) {
      if (canonicalTarget[i] == canonicalBase[i]) {
        if (canonicalTarget[i] == SLASH || canonicalTarget[i] == BACKSLASH) {
          lastSeparator = i
        }
      } else {
        firstDifference = lastSeparator + 1
        break
      }
    }
    val relativePath = StringBuilder(50)

    //
    // walk from the first difference to the end of the base
    // adding "../" for each separator encountered
    //
    if (canonicalBase.length > firstDifference) {
      relativePath.append("..")
      for (i in firstDifference until canonicalBase.length) {
        if (canonicalBase[i] == SLASH || canonicalBase[i] == BACKSLASH) {
          relativePath.append(separator)
          relativePath.append("..")
        }
      }
    }
    if (canonicalTarget.length > firstDifference) {
      //
      // append the rest of the target
      //
      if (relativePath.length > 0) {
        relativePath.append(separator)
      }
      relativePath.append(canonicalTarget.substring(firstDifference))
    }
    return relativePath.toString()
  }

  /**
   *
   * getRelativePath
   *
   * @param target        a File object.
   * @param base          a File object.
   * @param pathSeparator a String object.
   * @return a File object.
   */
  @JvmStatic
  fun getRelativePath(target: File, base: File, pathSeparator: String): File {
    return File(getRelativePath(target.path, base.path, pathSeparator))
  }

  /**
   *
   * getRelativePath
   *
   * @param target a File object.
   * @param base   a File object.
   * @return a File object.
   */
  @JvmStatic
  fun getRelativePath(target: File, base: File): File {
    return getRelativePath(target, base, File.separator)
  }

  /**
   *
   * getRelativePath
   *
   * @param targetPath a String object.
   * @param basePath   a String object.
   * @return a String object.
   */
  @JvmStatic
  fun getRelativePath(targetPath: String, basePath: String): String {
    return getRelativePath(targetPath, basePath, File.separator)
  }
}
