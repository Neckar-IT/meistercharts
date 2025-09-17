package it.neckar.open.test.utils

import assertk.*
import com.fasterxml.jackson.databind.JsonNode
import it.neckar.open.file.requireIsFile
import it.neckar.open.kotlin.lang.ExecutionEnvironment
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

      if (ExecutionEnvironment.inCI.not()) {
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
