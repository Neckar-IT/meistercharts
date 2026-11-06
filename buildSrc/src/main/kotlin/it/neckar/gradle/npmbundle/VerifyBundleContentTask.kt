package it.neckar.gradle.npmbundle

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task to verify the plausibility of the bundle content.
 * Ensures that the directory contains at least one `.js` file.
 */
abstract class VerifyBundleContentTask : DefaultTask() {

  @get:InputDirectory
  abstract val workingDir: DirectoryProperty

  @TaskAction
  fun verify() {
    val workingDirFile = workingDir.get().asFile
    logger.info("Verifying bundle content in $workingDirFile")

    val jsFilesExist = workingDir.asFileTree.any { it.extension == "js" }

    if (jsFilesExist.not()) {
      throw GradleException("Verification failed: No .js files found in $workingDirFile")
    }

    logger.info("Verification successful: Bundle contains .js files in [${workingDirFile.absolutePath}]")
  }
}
