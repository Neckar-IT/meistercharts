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
