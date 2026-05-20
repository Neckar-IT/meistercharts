/**
 * Compatibility functions for the standalone meistercharts.com-sync build.
 *
 * These functions are defined in GradleProjectUtils.kt in the monorepo buildSrc,
 * but that file cannot be copied directly because it references monorepo-specific
 * infrastructure (Docker images, closed projects). This file provides the subset
 * of functions needed by ProjectConfiguration.kt, Utils.kt, and NpmBundlePlugin.kt.
 */

import it.neckar.gradle.Plugins
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.gradle.api.GradleException
import org.gradle.process.ExecOutput
import org.gradle.kotlin.dsl.extra
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.UnknownDomainObjectException
import java.util.Properties

fun Project.hasKotlinMultiplatformPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinMultiPlatform)
}

fun Project.hasKotlinJvmPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinJvm)
}

fun Project.hasPlugin(pluginId: String): Boolean {
  return this.pluginManager.findPlugin(pluginId) != null
}

fun <T : Any> NamedDomainObjectSet<T>.findNamed(name: String): org.gradle.api.NamedDomainObjectProvider<T>? {
  return try {
    this.named(name)
  } catch (_: UnknownDomainObjectException) {
    null
  }
}

fun Project.parsePackageJson(): JsonElement {
  val packageTemplateJson = file("package.template.json")

  if (packageTemplateJson.isFile.not()) {
    throw GradleException("Expected ${packageTemplateJson.absolutePath} to be a file")
  }

  return Json.parseToJsonElement(packageTemplateJson.readText())
}

fun Project.packageJsonContainsScript(scriptName: String): Boolean {
  return (parsePackageJson().jsonObject["scripts"]?.jsonObject?.containsKey(scriptName) == true)
}

private val Project.envProperties: Properties
  get() {
    val envFile = rootProject.file(".env")
    return Properties().apply {
      if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
      }
    }
  }

fun Project.resolveConfigValueOrNull(propertyName: String): String? {
  return findProperty(propertyName)?.toString()
    ?: System.getenv(propertyName)
    ?: envProperties.getProperty(propertyName)
}

fun Project.resolveConfigValue(propertyName: String): String {
  return resolveConfigValueOrNull(propertyName)
    ?: throw GradleException("$propertyName not set (via -P flag, environment variable, or .env file)")
}

fun Project.resolveConfigBoolean(propertyName: String): Boolean {
  return resolveConfigValueOrNull(propertyName) == "true"
}

fun Project.resolveConfigBooleanOrNull(propertyName: String): Boolean? {
  return resolveConfigValueOrNull(propertyName)?.let { it == "true" }
}

fun Project.isDisabledProject(): Boolean {
  return false
}

// Extension properties for build variables set via rootProject.extra in root build.gradle.kts.
// These make the extra properties accessible as typed properties in subproject build scripts.

val Project.gitCommit: String
  get() = rootProject.extra.get("gitCommit") as String

val Project.gitCommitDate: String
  get() = rootProject.extra.get("gitCommitDate") as String

val Project.gitDescribe: String
  get() = rootProject.extra.get("gitDescribe") as String

val Project.buildDateDay: String
  get() = rootProject.extra.get("buildDateDay") as String

// Exec output helpers from GradleProjectUtils.kt

fun ExecOutput.standardOutputAsStringOnSuccess(): String {
  return standardOutputAsBytesOnSuccess().decodeToString()
}

fun ExecOutput.standardOutputAsBytesOnSuccess(): ByteArray {
  val result = result.get()
  result.rethrowFailure()
  return standardOutput.asBytes.get()
}
