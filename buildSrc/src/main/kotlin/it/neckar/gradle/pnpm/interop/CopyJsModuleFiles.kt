package it.neckar.gradle.pnpm.interop

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Copies the JS module files
 */
abstract class CopyJsModuleFiles : DefaultTask() {
  /**
   * The source directory where the js module files are located
   */
  @get:InputDirectory
  abstract val sourceDirectory: DirectoryProperty

  /**
   * The target directory where the JS module files are copied to
   */
  @get:OutputDirectory
  abstract val targetDirectory: DirectoryProperty

  @TaskAction
  fun copy() {
    project.copy {
      from(sourceDirectory)
      include("*.mjs*", "*.map")
      into(targetDirectory)

      duplicatesStrategy = DuplicatesStrategy.FAIL
    }
  }
}
