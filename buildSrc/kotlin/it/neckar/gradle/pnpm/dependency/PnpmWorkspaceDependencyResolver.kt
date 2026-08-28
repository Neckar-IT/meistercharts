package it.neckar.gradle.pnpm.dependency

import it.neckar.projects.GradleProjectPath
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

/**
 * Maps the `workspace:` dependencies a pnpm module declares in its `package.json` to the Gradle
 * project paths of the modules providing them.
 */
class PnpmWorkspaceDependencyResolver(
  private val packageNameRegistry: PackageNameRegistry = PackageNameRegistry.create(),
  private val packageJsonParser: PackageJsonParser = PackageJsonParser(),
) {
  fun resolveWorkspaceDependenciesByType(project: Project): ResolvedWorkspaceDependencies {
    val workspaceDeps = packageJsonParser.parse(packageJsonOf(project)).workspaceDependencies

    return ResolvedWorkspaceDependencies(
      dependencies = resolvePackageNames(workspaceDeps.dependencies),
      devDependencies = resolvePackageNames(workspaceDeps.devDependencies),
    )
  }

  /**
   * A pnpm module without a `package.json` cannot declare what it consumes, so an absent file is a
   * defect rather than an empty dependency list — [PackageNameRegistry.create] already refuses to
   * build a registry for such a module.
   */
  private fun packageJsonOf(project: Project): File {
    val packageJsonFile = project.file("package.json")

    if (packageJsonFile.isFile.not()) {
      throw GradleException("No package.json in pnpm module '${project.path}', expected at ${packageJsonFile.absolutePath}")
    }

    return packageJsonFile
  }

  private fun resolvePackageNames(packageNames: List<NpmPackageName>): List<GradleProjectPath> {
    return packageNames
      .map { packageName -> packageNameRegistry.findGradlePath(packageName) }
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
