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
     * and extracting their package names from package.template.json files.
     *
     * Uses template files to avoid dependency on generated files during Gradle configuration phase.
     *
     * @param pnpmProjects List of pnpm projects to scan (defaults to all pnpm projects)
     */
    fun create(
      pnpmProjects: List<ConfiguredProject> = Projects.pnpmProjects(),
    ): PackageNameRegistry {
      val parser = PackageJsonParser()

      val mapping = pnpmProjects
        .associate { configuredProject ->
          val packageTemplateFile = configuredProject.project().file("package.template.json")

          if (packageTemplateFile.exists().not()) {
            throw GradleException("package.template.json not found for pnpm project '${configuredProject.path}' at ${packageTemplateFile.absolutePath}")
          }

          val packageName = parser.extractPackageName(packageTemplateFile)
            ?: throw GradleException("No 'name' field found in ${packageTemplateFile.absolutePath}")

          logger.debug("Mapped package '$packageName' -> '${configuredProject.path}'")
          packageName to configuredProject.path
        }

      logger.info("Built package name registry with ${mapping.size} entries")
      return PackageNameRegistry(mapping)
    }
  }
}
