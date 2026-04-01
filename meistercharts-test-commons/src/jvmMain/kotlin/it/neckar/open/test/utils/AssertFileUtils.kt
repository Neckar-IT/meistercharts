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
package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEach
import it.neckar.open.crypt.Hash
import it.neckar.open.crypt.HashAlgorithm
import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File

object AssertFileUtils {
  @JvmStatic
  fun assertFileByHashes(fileUnderTest: File, algorithm: HashAlgorithm, vararg expectedHashesAsHex: String) {
    val expectedHashes = Array(expectedHashesAsHex.size) {
      val expectedHashAsHex = expectedHashesAsHex[it]
      Hash.fromHex(algorithm, expectedHashAsHex)
    }

    assertFileByHashes(fileUnderTest, *expectedHashes)
  }

  @JvmStatic
  fun assertFileByHashes(fileUnderTest: File, vararg expectedHashes: Hash) {
    require(expectedHashes.isNotEmpty()) { "Need at least on hash" }
    assertFileByHash(AssertUtils.guessPathFromStackTrace(), listOf(*expectedHashes), fileUnderTest)
  }

  @JvmStatic
  fun assertFileByHash(expected: Hash, fileUnderTest: File) {
    val path = AssertUtils.guessPathFromStackTrace()
    assertFileByHash(path, expected, fileUnderTest)
  }

  @JvmStatic
  fun assertFileByHash(testClass: Class<*>, testMethodName: String, expected: Hash, fileUnderTest: File) {
    assertFileByHash(createPath(testClass, testMethodName), expected, fileUnderTest)
  }

  @JvmStatic
  fun createPath(testClass: Class<*>, testMethodName: String): String {
    return testClass.name + File.separator + testMethodName
  }

  @JvmStatic
  fun assertFileByHash(path: String, expected: Hash, fileUnderTest: File) {
    val actual = expected.algorithm.calculate(file = fileUnderTest)
    if (expected == actual) {
      return  //everything went fine
    }
    val copy = createCopyFile(path, fileUnderTest.name)
    if (copy.exists()) {
      FileUtils.moveFile(copy, File(copy.parentFile, copy.name + "." + System.nanoTime()))
    }
    FileUtils.copyFile(fileUnderTest, copy)
    assertThat(actual, createReason(copy)).isEqualTo(expected)
  }


  private fun createReason(copy: File): String {
    return "Stored questionable file under test at <" + copy.absolutePath + ">"
  }

  @JvmStatic
  fun assertFileByHash(path: String, expectedHashes: Iterable<Hash>, fileUnderTest: File) {
    val actualHashes: MutableCollection<Hash> = ArrayList()
    for (expected in expectedHashes) {
      val actual = expected.algorithm.calculate(file = fileUnderTest)
      actualHashes.add(actual)
      if (expected == actual) {
        return  //everything went fine
      }
    }
    val copy = createCopyFile(path, fileUnderTest.name)
    FileUtils.copyFile(fileUnderTest, copy)
    assertThat(actualHashes, createReason(copy)).isEqualTo(expectedHashes)
  }


  @JvmStatic
  fun createCopyFile(path: String, name: String): File {
    return File(File(AssertUtils.FailedFilesDir, path), name)
  }
}

fun Assert<File>.containsOnlyFiles(vararg relativeFilePaths: String): Unit = given { dir ->
  assertThat(dir).isDirectory()

  val filePaths: List<String> = relativeFilePaths.toList()

  val files = FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE)
  assertThat(files.size).isEqualTo(filePaths.size)

  //Create the set with the expected files
  val expected = filePaths.map { File(dir, it) }.toSet()
  for (file in files) {
    assertThat(expected).contains(file)
  }
}

fun Assert<File>.containsFiles(vararg relativeFilePaths: String): Unit = given { dir ->
  assertThat(dir).isDirectory()

  relativeFilePaths.map { File(dir, it) }.fastForEach { file ->
    assertThat(file).exists()
  }
}

fun Assert<File>.isEmptyDirectory(): Unit = given { dir ->
  assertThat(dir).isDirectory()
  val listing = requireNotNull(dir.list())
  assertThat(listing).isEmpty()
}

