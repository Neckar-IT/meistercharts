/*
 * MIT License
 * <p>
 * Copyright (c) 2017 Ralf Stuckert
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package it.neckar.open.test.utils

import java.io.File
import java.io.IOException
import java.util.Locale

/**
 * Represents a temporary folder
 */
class TemporaryFolder {

  private var root: File? = null

  fun getRoot(): File {
    return root ?: createTemporaryFolder(null).also {
      this.root = it
    }
  }

  fun delete() {
    root?.let {
      recursiveDelete(it)
      root = null
    }
  }

  fun newFile(fileName: String): File {
    val file = File(getRoot(), fileName)
    if (!file.createNewFile()) {
      throw IOException(String.format(Locale.US, "failed to create file %s in folder %s", fileName, getRoot()))
    }
    return file
  }

  fun newFile(): File {
    return File.createTempFile("junit", null, getRoot())
  }

  fun newFolder(): File {
    return createTemporaryFolder(getRoot())
  }

  fun newFolder(name: String): File {
    val folder = File(getRoot(), name)
    folder.mkdir()
    return folder
  }

  companion object {
    @JvmStatic
    private fun createTemporaryFolder(base: File?): File {
      val createdFolder = File.createTempFile("junit", "", base)
      createdFolder.delete()
      createdFolder.mkdir()
      return createdFolder
    }

    private fun recursiveDelete(file: File) {
      val files = file.listFiles()
      if (files != null) {
        for (each in files) {
          recursiveDelete(each)
        }
      }
      if (!file.delete()) {
        file.deleteOnExit()
      }
    }
  }
}
