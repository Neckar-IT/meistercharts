package it.neckar.gradle.pnpm.dependencies

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile

/**
 * Extension for the `install pnpm dependency` plugin
 */
open class InstallPnpmDependencyPluginExtension(objects: ObjectFactory) {
  /**
   * The dependency file (usually `npmDependencies.json`) for the Gradle dependency project.
   * This property is only relevant if this is the Gradle dependency project.
   */
  @Optional
  @OutputFile
  val dependencyFile: RegularFileProperty = objects.fileProperty()

  /**
   * The path to the package.template.json file.
   */
  @OutputFile
  val packageJsonTemplateFile: RegularFileProperty = objects.fileProperty()
}
