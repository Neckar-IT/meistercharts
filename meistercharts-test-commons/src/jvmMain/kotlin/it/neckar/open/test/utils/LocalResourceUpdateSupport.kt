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
   * The source of the resource file that is verified
   */
  val resourceSource: ResourceSource,
) {

  /**
   * Returns the content of the resource file
   */
  fun loadResourceContentAsString(): String {
    return resourceSource.loadResourceContentAsString()
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
        resourceSource.writeText(updatedContentProvider())
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

  companion object {
    /**
     * Creates a new instance that updates the provided file if necessary
     */
    operator fun invoke(file: File, generateIfNotExists: Boolean = false): LocalResourceUpdateSupport {
      val fileSource = ResourceSource.FileSource(file, generateIfNotExists)

      return LocalResourceUpdateSupport(fileSource)
    }
  }

  /**
   * Interface that defines the source of a resource file.
   */
  sealed interface ResourceSource {
    /**
     * Loads the content of the resource file as a string.
     */
    fun loadResourceContentAsString(): String

    /**
     * Writes the updated content to the resource file.
     * This method is only called if the content does not match the expected content.
     */
    fun writeText(updatedContent: String)

    /**
     * A [ResourceSource] that reads from a file.
     */
    class FileSource(val file: File, generateIfNotExists: Boolean) : ResourceSource {
      init {
        if (generateIfNotExists) {
          if (file.exists().not()) {
            require(file.createNewFile()) {
              "Could not create file: ${file.absolutePath}"
            }
          }
        }

        file.requireIsFile()
      }

      override fun loadResourceContentAsString(): String {
        return file.readText()
      }

      override fun writeText(updatedContent: String) {
        println("#######################")
        println("#######################")
        println("Writing updated content to file: ${file.absolutePath}")
        println("#######################")
        println("#######################")

        file.writeText(updatedContent)
      }
    }
  }
}
