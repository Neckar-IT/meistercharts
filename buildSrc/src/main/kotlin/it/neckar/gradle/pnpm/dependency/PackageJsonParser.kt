package it.neckar.gradle.pnpm.dependency

import it.neckar.gradle.requireFileExists
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.gradle.api.GradleException
import java.io.File

/**
 * Represents an npm package name.
 */
@JvmInline
value class NpmPackageName(val name: String) {
  override fun toString(): String = name
}

/**
 * Parses package.json files to extract package metadata and workspace dependencies.
 */
class PackageJsonParser {
  private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

  /** Reads and parses [packageJsonFile]; every value is then taken from the result. */
  fun parse(packageJsonFile: File): ParsedPackageJson {
    packageJsonFile.requireFileExists { "No package.json at ${it?.absolutePath}" }

    val rootElement = try {
      json.parseToJsonElement(packageJsonFile.readText())
    } catch (e: SerializationException) {
      // The kotlinx message names an offset and a path, never the file — one of fifty manifests.
      throw GradleException("Malformed package.json at ${packageJsonFile.absolutePath}: ${e.message}", e)
    }

    return ParsedPackageJson(packageJsonFile, rootElement.asObject(packageJsonFile, "The manifest"))
  }

}

/**
 * A package.json read and parsed once. Every value is derived here and not on each access, so a
 * caller may read the same one repeatedly.
 *
 * A section of the wrong JSON type fails naming [source] and the section — kotlinx reports neither.
 */
class ParsedPackageJson internal constructor(private val source: File, jsonObject: JsonObject) {
  private val scripts: JsonObject? = jsonObject["scripts"]?.asObject(source, "'scripts'")

  /** Null where the manifest declares no `name`; whether that is a defect is the caller's to decide. */
  val name: NpmPackageName? = jsonObject["name"]?.asString(source, "'name'")?.let { NpmPackageName(it) }

  val scriptNames: Set<String> = scripts?.keys.orEmpty()

  /** Dependencies whose version specifier starts with `workspace:`, by section. */
  val workspaceDependencies: WorkspaceDependencies = WorkspaceDependencies(
    dependencies = jsonObject.workspaceDependenciesOf(source, "dependencies"),
    devDependencies = jsonObject.workspaceDependenciesOf(source, "devDependencies"),
  )

  fun script(scriptName: String): String? = scripts?.get(scriptName)?.asString(source, "script '$scriptName'")
}

private fun JsonObject.workspaceDependenciesOf(source: File, section: String): List<NpmPackageName> {
  val deps = this[section]?.asObject(source, "'$section'") ?: return emptyList()

  return deps.entries
    .filter { (packageName, versionElement) ->
      versionElement.asString(source, "the version of '$packageName' in '$section'").startsWith("workspace:")
    }
    .map { (packageName, _) -> NpmPackageName(packageName) }
}

private fun JsonElement.asObject(source: File, what: String): JsonObject {
  return this as? JsonObject
    ?: throw GradleException("$what in ${source.absolutePath} must be an object, but is ${this::class.simpleName}")
}

private fun JsonElement.asString(source: File, what: String): String {
  return (this as? JsonPrimitive)?.takeIf { it.isString }?.content
    ?: throw GradleException("$what in ${source.absolutePath} must be a string, but is $this")
}

/**
 * Holds workspace dependencies separated by type.
 */
data class WorkspaceDependencies(
  /** Runtime dependencies from the "dependencies" section */
  val dependencies: List<NpmPackageName>,
  /** Development dependencies from the "devDependencies" section */
  val devDependencies: List<NpmPackageName>,
) {
  /** Both sections in one list, each package once. */
  val all: List<NpmPackageName> = (dependencies + devDependencies).distinct()
}
