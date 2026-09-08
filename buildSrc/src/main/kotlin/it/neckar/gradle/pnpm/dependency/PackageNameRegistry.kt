package it.neckar.gradle.pnpm.dependency

import it.neckar.projects.ConfiguredProject
import it.neckar.projects.GradleProjectPath
import it.neckar.projects.OtherProjects
import it.neckar.projects.Projects
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

/**
 * Registry that maps npm package names to their corresponding Gradle project paths.
 */
class PackageNameRegistry internal constructor(
  private val packageNameToGradlePath: Map<NpmPackageName, GradleProjectPath>,
) {
  /**
   * The Gradle project path for [packageName].
   *
   * An unknown package is a defect, not an absence: dropping it removes the build-order edge and the
   * dependency's `dist/` input, leaving the consuming module up-to-date after its dependency changed.
   */
  fun findGradlePath(packageName: NpmPackageName): GradleProjectPath {
    return findGradlePathOrNull(packageName)
      ?: throw GradleException(
        "No pnpm module provides npm package '$packageName'. Either it is not registered as a Gradle " +
          "project (settings.gradle.kts + Projects.kt), or its package.json declares a different 'name'. " +
          "Registered packages: $size."
      )
  }

  fun findGradlePathOrNull(packageName: NpmPackageName): GradleProjectPath? = packageNameToGradlePath[packageName]

  /**
   * Returns the number of packages in the registry.
   */
  val size: Int = packageNameToGradlePath.size

  companion object {
    private val logger = Logging.getLogger(PackageNameRegistry::class.java)

    /**
     * Creates a new [PackageNameRegistry] by scanning all pnpm projects
     * and extracting their package names from `package.json` files.
     *
     * @param pnpmProjects List of pnpm projects to scan (defaults to all pnpm projects)
     */
    fun create(
      pnpmProjects: List<ConfiguredProject> = Projects.pnpmProjects() + OtherProjects.pnpmProjects(),
    ): PackageNameRegistry {
      val parser = PackageJsonParser()
      val mapping = mutableMapOf<NpmPackageName, GradleProjectPath>()

      pnpmProjects.forEach { configuredProject ->
        val packageJsonFile = configuredProject.project().file("package.json")

        val packageName = parser.parse(packageJsonFile).name
          ?: throw GradleException(
            "pnpm project '${configuredProject.path}' declares no 'name' in ${packageJsonFile.absolutePath}."
          )

        // put returns the previous holder — a second module claiming one name would otherwise
        // silently take over the mapping and misdirect every dependency on it.
        mapping.put(packageName, configuredProject.path)?.let { alreadyClaimedBy ->
          throw GradleException(
            "pnpm projects '${configuredProject.path}' and '$alreadyClaimedBy' both declare the npm package name '$packageName'."
          )
        }

        logger.debug("Mapped package '$packageName' -> '${configuredProject.path}'")
      }

      logger.info("Built package name registry with ${mapping.size} entries")
      return PackageNameRegistry(mapping)
    }
  }
}
