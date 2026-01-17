package it.neckar.gradle.pnpm.dependencies

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory

/**
 * Extension for the `install pnpm dependency` plugin
 */
open class InstallPnpmDependencyPluginExtension(objects: ObjectFactory) {
  /**
   * The dependency file (usually `npmDependencies.json`) for the Gradle dependency project.
   * This property is only relevant if this is the Gradle dependency project.
   */
  val dependencyFile: RegularFileProperty = objects.fileProperty()

  /**
   * The path to the package.template.json file.
   */
  val packageJsonTemplateFile: RegularFileProperty = objects.fileProperty()
}
