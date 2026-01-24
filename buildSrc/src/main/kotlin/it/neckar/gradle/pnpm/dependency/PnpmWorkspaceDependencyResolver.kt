package it.neckar.gradle.pnpm.dependency

import GradleProjectPath
import org.gradle.api.Project
import org.gradle.api.logging.Logging

/**
 * Resolves workspace dependencies for pnpm projects.
 *
 * Parses package.json files to extract internal workspace dependencies
 * (dependencies with `workspace:*` specifier) and maps them to Gradle project paths.
 *
 * ## Architecture
 * The resolver consists of three components:
 * 1. [PackageNameRegistry] - Maps npm package names to Gradle project paths
 * 2. [PackageJsonParser] - Parses package.json files to extract workspace dependencies
 * 3. [GradleProjectPath] - Value class for type-safe Gradle project paths
 *
 * ## Usage
 * ```kotlin
 * val resolver = PnpmWorkspaceDependencyResolver()
 * val dependencies = resolver.resolveWorkspaceDependencies(project)
 * // Returns: [GradleProjectPath(":internal:open:commons:typescript-utils"), ...]
 * ```
 */
class PnpmWorkspaceDependencyResolver(
  private val packageNameRegistry: PackageNameRegistry = PackageNameRegistry.create(),
  private val packageJsonParser: PackageJsonParser = PackageJsonParser(),
) {
  private val logger = Logging.getLogger(PnpmWorkspaceDependencyResolver::class.java)

  /**
   * Determines all internal workspace dependencies of a pnpm project.
   *
   * Parses the project's package.json file and extracts dependencies that use
   * the `workspace:*` specifier, then maps them to their corresponding Gradle project paths.
   *
   * @return List of Gradle project paths for workspace dependencies
   */
  fun resolveWorkspaceDependencies(project: Project): List<GradleProjectPath> {
    val packageJsonFile = project.file("package.json")

    if (packageJsonFile.exists().not()) {
      logger.debug("No package.json found for project ${project.path}")
      return emptyList()
    }

    val workspacePackageNames = packageJsonParser.extractWorkspaceDependencyNames(packageJsonFile)

    return resolvePackageNames(workspacePackageNames, project)
  }

  /**
   * Determines workspace dependencies of a pnpm project, separated by type.
   *
   * @return [ResolvedWorkspaceDependencies] with separate lists for dependencies and devDependencies
   */
  fun resolveWorkspaceDependenciesByType(project: Project): ResolvedWorkspaceDependencies {
    val packageJsonFile = project.file("package.json")

    if (packageJsonFile.exists().not()) {
      logger.debug("No package.json found for project ${project.path}")
      return ResolvedWorkspaceDependencies(emptyList(), emptyList())
    }

    val workspaceDeps = packageJsonParser.extractWorkspaceDependenciesByType(packageJsonFile)

    return ResolvedWorkspaceDependencies(
      dependencies = resolvePackageNames(workspaceDeps.dependencies, project),
      devDependencies = resolvePackageNames(workspaceDeps.devDependencies, project),
    )
  }

  private fun resolvePackageNames(packageNames: List<NpmPackageName>, project: Project): List<GradleProjectPath> {
    return packageNames
      .mapNotNull { packageName ->
        packageNameRegistry.findGradlePath(packageName).also { gradlePath ->
          if (gradlePath == null) {
            logger.warn("Could not resolve workspace dependency '$packageName' in ${project.path} to a Gradle project path")
          }
        }
      }
      .distinctBy { it.path }
      .sortedBy { it.path }
  }
}

/**
 * Holds resolved workspace dependencies separated by type.
 */
data class ResolvedWorkspaceDependencies(
  /** Runtime dependencies from the "dependencies" section */
  val dependencies: List<GradleProjectPath>,
  /** Development dependencies from the "devDependencies" section */
  val devDependencies: List<GradleProjectPath>,
)
