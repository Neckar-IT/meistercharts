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
import com.fasterxml.jackson.databind.JsonNode
import it.neckar.open.file.requireIsFile
import it.neckar.runtime.context.RuntimeContext
import java.io.File

/**
 * Support class that verifies that a local resource is up-to-date.
 * This can be used to ensure that a checked in file is regenerated without any differences
 */
class LocalResourceUpdateSupport(
  /**
   * The file that is verified
   */
  val file: File,

  val generateIfNotExists: Boolean = false,
) {

  private val generationSupport: LocalResourceGenerationSupport = LocalResourceGenerationSupport(file)

  init {
    if (generateIfNotExists) {
      //Create the file as soon as possible to detect file related problems asap
      if (file.exists().not()) {
        file.parentFile?.mkdirs()
        require(file.createNewFile()) {
          "Could not create file: ${file.absolutePath}"
        }
      }
    }

    //Ensure the file exists, fail immediately if not
    file.requireIsFile()
  }

  /**
   * Returns the content of the resource file
   */
  fun loadResourceContentAsString(): String {
    return file.readText()
  }

  /**
   * Loads the resources, verifies that it matches the content and updates the file if necessary
   */
  fun assertContentMatches(
    /**
     * Is called with the stored content.
     * Should throw a [java.lang.AssertionError] if the content does not match
     */
    compare: (storedContent: String) -> Unit,
    /**
     * Provides the updated content. Is only called if [compare] throws a [java.lang.AssertionError]
     */
    updatedContentProvider: () -> String,
  ) {
    val storedContent = loadResourceContentAsString()

    try {
      compare(storedContent)
    } catch (e: java.lang.AssertionError) {

      if (RuntimeContext.inCI.not()) {
        //save the new content - if *not* in CI
        println("#######################")
        println("#######################")
        println("Writing updated content to file: ${file.absolutePath}")
        println("#######################")
        println("#######################")

        generationSupport.generate(updatedContentProvider)
      }

      throw e
    }
  }

  /**
   * Asserts that the content of the resource file is equal to the expected json
   */
  fun assertJsonEquals(expectedJson: String, fileName: String? = null, actualTreeModifier: JsonNode.() -> Unit = {}) {
    assertContentMatches(
      compare = { storedContent ->
        assertThat(storedContent, fileName).isJsonEqualTo(expectedJson, actualTreeModifier)
      },
      updatedContentProvider = {
        //Reformat the json
        JsonUtils.formatJson(expectedJson)
      }
    )
  }

  /**
   * Asserts that all lines of the content are equal to the lines expected string
   */
  fun assertLinesEqual(expectedString: String) {
    assertContentMatches(
      compare = { storedContent ->
        assertThat(storedContent).isEqualComparingLinesTrim(expectedString)
      },
      updatedContentProvider = {
        expectedString
      }
    )
  }

  fun assertDiffIgnoreWhitespaces(expectedString: String) {
    assertContentMatches(
      compare = { storedContent ->
        assertThat(storedContent).isEqualIgnoringWhitespacesEmptyLines(expectedString)
      },
      updatedContentProvider = {
        expectedString
      }
    )
  }
}
