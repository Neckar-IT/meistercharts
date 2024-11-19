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
package it.neckar.open.test.utils.matchers

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File
import java.util.Collections
import java.util.function.Predicate

/**
 *
 */
class ContainsOnlyFilesMatcher(vararg relativeFilePaths: String) : Predicate<File> {
  private val filePaths: List<String> = relativeFilePaths.toList()

  override fun test(dir: File): Boolean {
    if (!dir.isDirectory) {
      return false
    }
    val files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
    if (files.size != filePaths.size) {
      return false
    }

    //Create the set with the expected files
    val expected = createExepectedSet(dir)
    for (file in files) {
      if (!expected.contains(file)) {
        return false
      }
    }
    return true
  }

  private fun createExepectedSet(baseDir: File): Set<File> {
    val expected: MutableSet<File> = HashSet()
    for (filePath in filePaths) {
      expected.add(File(baseDir, filePath))
    }
    return expected
  }

  fun getFilePaths(): List<String> {
    return Collections.unmodifiableList(filePaths)
  }

  companion object {
    @JvmStatic
    fun containsOnlyFiles(vararg relativeFilePaths: String): Predicate<File> {
      return ContainsOnlyFilesMatcher(*relativeFilePaths)
    }

    @JvmStatic
    fun toTree(dir: File): String {
      return ContainsFileMatcher.toTree(dir)
    }
  }
}
