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
