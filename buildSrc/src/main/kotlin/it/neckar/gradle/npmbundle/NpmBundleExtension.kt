package it.neckar.gradle.npmbundle

import org.gradle.api.file.CopySpec
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property

/**
 * Extension for npm bundle
 */
interface NpmBundleExtension {
  /**
   * The name of the published package - will be used as name for the package.json
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
   * The version number - will be used as version for the package.json
   */
  val version: Property<String>


  /**
   * The files to bundle - will be copied to the working directory
   */
  val filesToBundle: Property<CopySpec>

  /**
   * The directory that holds the unpacked npm module.
   */
  val workingDir: DirectoryProperty

  /**
   * The directory that contains the tar.gz that contains the content of [workingDir]
   */
  val targetDirectoryForArchive: DirectoryProperty
}
