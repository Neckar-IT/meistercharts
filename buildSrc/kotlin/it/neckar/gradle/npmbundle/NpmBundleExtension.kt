package it.neckar.gradle.npmbundle

import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property

/**
 * Extension for npm bundle
 */
interface NpmBundleExtension {
  /**
   * The module name - will be used as name for the package.json
   */
  val moduleName: Property<String>

  /**
   * The name of the directory within the tar.gz file
   */
  val dirNameInArchive: Property<String>

  /**
   * The name of the archive file - *without* suffix
   */
  val archiveFileName: Property<String>

  /**
   * The version number - will be used when processing the package.json template
   */
  val version: Property<String>


  /**
   * The files to bundle - will be copied to the working directory
   */
  val filesToBundle: Property<CopySpec>

  /**
   * The npm module directory (usually build/npm).
   * Also contains the module name
   */
  val workingDir: DirectoryProperty

  /**
   * The package json template file. Will be copied to [workingDir]
   */
  val packageJsonTemplate: RegularFileProperty

  /**
   * The directory that contains the tar.gz that contains the package.json and the content provided by [copyContent]
   */
  val targetDirectoryForArchive: DirectoryProperty
}
