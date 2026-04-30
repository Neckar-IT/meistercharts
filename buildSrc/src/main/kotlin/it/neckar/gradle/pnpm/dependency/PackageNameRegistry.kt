package it.neckar.gradle.pnpm.dependency

import ConfiguredProject
import GradleProjectPath
import Projects
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

/**
 * Registry that maps npm package names to their corresponding Gradle project paths.
 *
 * Use the companion [create] factory function to build the registry.
 */
class PackageNameRegistry private constructor(
  private val packageNameToGradlePath: Map<NpmPackageName, GradleProjectPath>,
) {
  private val logger = Logging.getLogger(PackageNameRegistry::class.java)

  /**
   * Finds the Gradle project path for the given npm package name, or null if not found.
   */
  fun findGradlePath(packageName: NpmPackageName): GradleProjectPath? = packageNameToGradlePath[packageName]

  /**
   * Returns the number of packages in the registry.
   */
  val size: Int get() = packageNameToGradlePath.size

  companion object {
    private val logger = Logging.getLogger(PackageNameRegistry::class.java)

    /**
     * Creates a new [PackageNameRegistry] by scanning all pnpm projects
     * and extracting their package names from `package.json` files.
     *
     * @param pnpmProjects List of pnpm projects to scan (defaults to all pnpm projects)
     */
    fun create(
      pnpmProjects: List<ConfiguredProject> = Projects.pnpmProjects(),
    ): PackageNameRegistry {
      val parser = PackageJsonParser()

      val mapping = pnpmProjects
        .associate { configuredProject ->
          val packageJsonFile = configuredProject.project().file("package.json")

          if (packageJsonFile.exists().not()) {
            throw GradleException("package.json not found for pnpm project '${configuredProject.path}' at ${packageJsonFile.absolutePath}")
          }

          val packageName = parser.extractPackageName(packageJsonFile)
            ?: throw GradleException("No 'name' field found in ${packageJsonFile.absolutePath}")

          logger.debug("Mapped package '$packageName' -> '${configuredProject.path}'")
          packageName to configuredProject.path
        }

      logger.info("Built package name registry with ${mapping.size} entries")
      return PackageNameRegistry(mapping)
    }
  }
}
