package it.neckar.ksp.ts

object DocumentationSupport {
  /**
   * Creates a typescript documentation string for a function or class.
   * Takes a Kotlin Doc string
   */
  fun createKotlinDoc(docString: String?, indentation: String): String? {
    if (docString.isNullOrBlank()) {
      return null
    }

    return buildString {
      appendLine("${indentation}/**")
      docString.trim().lines().forEach { line ->
        appendLine("$indentation * ${line.trim()}")
      }
      appendLine("$indentation */")
    }.trimEnd()
  }
}
