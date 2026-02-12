package it.neckar.open.version

import java.util.Properties

/**
 * JVM implementation: resolves git info from git.properties resource file.
 * This avoids Kotlin recompilation when only the git hash changes.
 */
internal actual fun resolveGitInfo(property: GitProperty): String {
  return gitProperties.getProperty(property.propertyKey)
    ?: throw IllegalStateException("Git property '${property.propertyKey}' not found in git.properties resource")
}

/**
 * Lazily loaded git properties from the classpath resource.
 */
private val gitProperties: Properties by lazy {
  val properties = Properties()
  val stream = VersionInformation::class.java.getResourceAsStream("/git.properties")
    ?: throw IllegalStateException("git.properties resource not found on classpath")
  stream.use { properties.load(it) }
  properties
}
