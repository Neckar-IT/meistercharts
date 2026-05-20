package it.neckar.gradle.pnpm.dependency

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

  /**
   * Extracts the package name from a package.json file.
   */
  fun extractPackageName(packageJsonFile: File): NpmPackageName? {
    val content = packageJsonFile.readText()
    val jsonObject = json.parseToJsonElement(content).jsonObject
    return jsonObject["name"]?.jsonPrimitive?.content?.let { NpmPackageName(it) }
  }

  /**
   * Extracts the names of all workspace dependencies from a package.json file.
   *
   * Workspace dependencies are identified by the `workspace:` prefix in their version specifier.
   * Both `dependencies` and `devDependencies` sections are checked.
   *
   * @return List of npm package names that are workspace dependencies
   */
  fun extractWorkspaceDependencyNames(packageJsonFile: File): List<NpmPackageName> {
    val content = packageJsonFile.readText()
    val jsonObject = json.parseToJsonElement(content).jsonObject

    return listOf("dependencies", "devDependencies")
      .flatMap { section ->
        val deps = jsonObject[section]?.jsonObject ?: return@flatMap emptyList()

        deps.entries
          .filter { (_, versionElement) ->
            versionElement.jsonPrimitive.content.startsWith("workspace:")
          }
          .map { (packageName, _) -> NpmPackageName(packageName) }
      }
      .distinct()
  }

  /**
   * Extracts workspace dependencies from a package.json file, separated by type.
   *
   * @return [WorkspaceDependencies] containing separate lists for dependencies and devDependencies
   */
  fun extractWorkspaceDependenciesByType(packageJsonFile: File): WorkspaceDependencies {
    val content = packageJsonFile.readText()
    val jsonObject = json.parseToJsonElement(content).jsonObject

    fun extractFromSection(section: String): List<NpmPackageName> {
      val deps = jsonObject[section]?.jsonObject ?: return emptyList()
      return deps.entries
        .filter { (_, versionElement) ->
          versionElement.jsonPrimitive.content.startsWith("workspace:")
        }
        .map { (packageName, _) -> NpmPackageName(packageName) }
    }

    return WorkspaceDependencies(
      dependencies = extractFromSection("dependencies"),
      devDependencies = extractFromSection("devDependencies"),
    )
  }
}

/**
 * Holds workspace dependencies separated by type.
 */
data class WorkspaceDependencies(
  /** Runtime dependencies from the "dependencies" section */
  val dependencies: List<NpmPackageName>,
  /** Development dependencies from the "devDependencies" section */
  val devDependencies: List<NpmPackageName>,
)
