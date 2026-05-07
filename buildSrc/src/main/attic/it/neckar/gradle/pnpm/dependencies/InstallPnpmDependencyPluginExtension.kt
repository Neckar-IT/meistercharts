package it.neckar.gradle.pnpm.dependencies

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory

/**
 * Extension for the `install pnpm dependency` plugin
 */
open class InstallPnpmDependencyPluginExtension(objects: ObjectFactory) {
  /**
   * The path to the package.template.json file.
   */
  val packageJsonTemplateFile: RegularFileProperty = objects.fileProperty()
}
