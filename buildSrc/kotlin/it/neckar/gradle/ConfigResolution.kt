package it.neckar.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project
import java.util.Properties

/**
 * Lazily loaded .env properties from the root project directory.
 */
private val Project.envProperties: Properties
  get() {
    val envFile = rootProject.file(".env")
    return Properties().apply {
      if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
      }
    }
  }

/**
 * Resolves a configuration value from multiple sources, returning null if not found.
 *
 * Resolution order:
 * 1. Gradle properties (-P flag)
 * 2. Environment variables
 * 3. .env file in project root
 */
fun Project.resolveConfigValueOrNull(
  propertyName: String,
): String? {
  return findProperty(propertyName)?.toString()
    ?: System.getenv(propertyName)
    ?: envProperties.getProperty(propertyName)
}

/**
 * Resolves a configuration value from multiple sources.
 *
 * Resolution order:
 * 1. Gradle properties (-P flag)
 * 2. Environment variables
 * 3. .env file in project root
 *
 * @throws GradleException if the property is not found in any source
 */
fun Project.resolveConfigValue(
  propertyName: String,
): String {
  return resolveConfigValueOrNull(propertyName)
    ?: throw GradleException("$propertyName not set (via -P flag, environment variable, or .env file)")
}

/**
 * Resolves a boolean configuration value from multiple sources.
 * Returns `true` if the resolved value equals "true" (case-sensitive), `false` otherwise.
 *
 * Resolution order: Gradle properties → environment variables → .env file
 */
fun Project.resolveConfigBoolean(propertyName: String): Boolean {
  return resolveConfigValueOrNull(propertyName) == "true"
}

/**
 * Resolves a boolean configuration value from multiple sources.
 * Returns `null` if the property is not set, allowing callers to distinguish
 * between "not configured" and "explicitly set to false".
 *
 * Resolution order: Gradle properties → environment variables → .env file
 */
fun Project.resolveConfigBooleanOrNull(propertyName: String): Boolean? {
  return resolveConfigValueOrNull(propertyName)?.let { it == "true" }
}
