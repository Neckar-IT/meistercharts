package it.neckar.logging.console

import it.neckar.open.kotlin.lang.fastFor
import kotlinx.browser.window

@JsExport
class LocalStorageFunctions(val prefix: String) {
  fun help() {
    println(
      """
      |Available functions:
      | * ${prefix}.list(): Lists the local storage configuration
      | * ${prefix}.clear(): Clears the local storage configuration for logs
      """.trimIndent()
    )
  }

  /**
   * Lists the local storage configuration
   */
  fun list() {
    println(buildString {
      append("Local storage:")

      window.localStorage.length.fastFor { index ->
        val key = window.localStorage.key(index) ?: throw IllegalStateException("Key at index $index is null")
        val value = window.localStorage.getItem(key)
        appendLine(" * $key: $value")
      }
    })
  }

  /**
   * Clears the root level from local storage
   */
  fun clear() {
    println("Clearing local storage configuration")
    window.localStorage.clear()
  }
}
