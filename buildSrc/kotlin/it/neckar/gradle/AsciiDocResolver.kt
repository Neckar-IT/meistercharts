package it.neckar.gradle

import org.gradle.kotlin.dsl.assign
import java.io.File

/**
 * Resolves all asciiDoc includes of the given asciidoc file.
 * Returns the resolved asciiDoc String
 */
fun resolveAdocIncludes(inputFile: File): String {
  val content = inputFile.readText()
  // regex pattern of asciidoc includes
  val includeRegex = Regex("""include::(.*?)\[\]""")
  val matches = includeRegex.findAll(content)
  var resolvedContent = content
  // iterate over all matched includes
  for (match in matches) {
    val includeFile = File(inputFile.parent, match.groupValues[1])
    val includeContent = includeFile.readText()
    resolvedContent = resolvedContent.replace(match.value, includeContent)
  }
  return resolvedContent
}
