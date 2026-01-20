package it.neckar.gradle.npmbundle

import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.property

/**
 * Extension for npm bundle
 */
open class NpmBundleExtension(objects: ObjectFactory) {
  /**
   * The module name - will be used as name for the package.json
   */
  val moduleName: Property<String> = objects.property()

  /**
   * The name of the directory within the tar.gz file
   */
  val dirNameInArchive: Property<String> = objects.property()

  /**
   * The name of the archive file - *without* suffix
   */
  val archiveFileName: Property<String> = objects.property()

  /**
   * The version number - will be used when processing the package.json template
   */
  val version: Property<String> = objects.property()


  /**
   * The files to bundle - will be copied to the working directory
   */
  val filesToBundle: Property<CopySpec> = objects.property<CopySpec>()

  /**
   * The npm module directory (usually build/npm).
   * Also contains the module name
   */
  val workingDir: DirectoryProperty = objects.directoryProperty()

  /**
   * The package json template file. Will be copied to [workingDir]
   */
  val packageJsonTemplate: RegularFileProperty = objects.fileProperty()

  /**
   * The directory that contains the tar.gz that contains the package.json and the content provided by [copyContent]
   */
  val targetDirectoryForArchive: DirectoryProperty = objects.directoryProperty()
}
