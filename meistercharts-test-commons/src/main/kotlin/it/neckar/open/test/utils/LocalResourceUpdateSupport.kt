package it.neckar.open.test.utils

import assertk.*
import assertk.assertions.*
import com.fasterxml.jackson.databind.JsonNode
import it.neckar.open.kotlin.lang.ExecutionEnvironment
import java.io.File

/**
 * Support class that verifies that a local resource is up-to-date.
 * This can be used to ensure that a checked in file is regenerated without any differences
 */
class LocalResourceUpdateSupport(
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
  fun assertJsonEquals(expectedJson: String, actualTreeModifier: JsonNode.() -> Unit = {}) {
    assertContentMatches(
      compare = { storedContent ->
        assertThat(storedContent).isJsonEqualTo(expectedJson, actualTreeModifier)
      },
      updatedContentProvider = {
        //Reformat the json
        JsonUtils.formatJson(expectedJson)
      }
    )
  }

  companion object {
    operator fun invoke(file: File): LocalResourceUpdateSupport {
      return LocalResourceUpdateSupport(ResourceSource.FileSource(file))
    }
  }


  sealed interface ResourceSource {
    fun loadResourceContentAsString(): String
    fun writeText(updatedContent: String)

    class FileSource(val file: File) : ResourceSource {

      init {
        assertThat(file).isFile()
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
