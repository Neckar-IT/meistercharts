package it.neckar.gradle.npmbundle

import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * Extension for npm bundle
 */
abstract class NpmBundleExtension {
  /**
   * The module name - will be used as name for the package.json
   */
  abstract val moduleName: Property<String>

  /**
   * The name of the directory within the tar.gz file
   */
  abstract val dirNameInArchive: Property<String>

  /**
   * The name of the archive file - *without* suffix
   */
  abstract val archiveFileName: Property<String>

  /**
   * The version number - will be used when processing the package.json template
   */
  abstract val version: Property<String>


  /**
   * The files to bundle - will be copied to the working directory
   */
  abstract val filesToBundle: Property<CopySpec>

  /**
   * The npm module directory (usually build/npm).
   * Also contains the module name
   */
  abstract val workingDir: DirectoryProperty

  /**
   * The package json template file. Will be copied to [workingDir]
   */
  abstract val packageJsonTemplate: RegularFileProperty

  /**
   * The directory that contains the tar.gz that contains the package.json and the content provided by [copyContent]
   */
  abstract val targetDirectoryForArchive: DirectoryProperty
}
