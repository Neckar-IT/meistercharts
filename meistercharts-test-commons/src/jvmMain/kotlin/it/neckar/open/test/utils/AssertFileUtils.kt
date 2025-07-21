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

