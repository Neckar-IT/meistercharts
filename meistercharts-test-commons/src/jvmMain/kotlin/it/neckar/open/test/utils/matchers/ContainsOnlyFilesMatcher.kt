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
