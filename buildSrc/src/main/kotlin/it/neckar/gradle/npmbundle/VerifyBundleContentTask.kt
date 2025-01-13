package it.neckar.gradle.npmbundle

import it.neckar.gradle.ansiConsole
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Task to verify the plausibility of the bundle content.
 * Ensures that the directory contains at least one `.js` file and a `package.json` file.
 */
abstract class VerifyBundleContentTask : DefaultTask() {

  @get:InputDirectory
  abstract val workingDir: DirectoryProperty

  @TaskAction
  fun verify() {
    val workingDirFile = workingDir.get().asFile
    project.logger.info("Verifying bundle content in $workingDirFile")

    val jsFilesExist = workingDir.asFileTree.any { it.extension == "js" }
    val packageJsonExists = workingDir.asFileTree.any { it.name == "package.json" }

    if (jsFilesExist.not()) {
      throw GradleException("Verification failed: No .js files found in $workingDirFile")
    }
    if (packageJsonExists.not()) {
      project.logger.lifecycle(ansiConsole.blue("Register the generatePackageJson plugin to generate a package.json file."))
      throw GradleException("Verification failed: No package.json file found in $workingDirFile")
    }

    project.logger.info("Verification successful: Bundle contains .js files and a package.json in [${workingDirFile.absolutePath}]")
  }
}
