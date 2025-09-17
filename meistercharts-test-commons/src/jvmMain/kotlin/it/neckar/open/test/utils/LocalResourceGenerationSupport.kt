package it.neckar.open.test.utils

import java.io.File

/**
 * Support class that *generates* local resource.
 *
 * This can be used to generate files from Unit tests.
 *
 * ATTENTION: This is an antipattern - but sometimes the only way if reflection is required to generate a file.
 * Prefer KSP or similar approaches to generate files if possible.
 */
class LocalResourceGenerationSupport(
  /**
   * target file
   */
  val target: File,
) {

  /**
   * Writes the content provided by the [contentProvider] to the target file.
   */
  inline fun generate(
    /**
     * Provides the content.
     */
    contentProvider: () -> String,
  ) {
    generate(contentProvider())
  }

  /**
   * Writes the given content to the target file.
   */
  fun generate(
    content: String,
  ) {
    target.parentFile?.mkdirs()
    target.writeText(content)
  }
}
