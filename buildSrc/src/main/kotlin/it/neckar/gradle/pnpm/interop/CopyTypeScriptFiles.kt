package it.neckar.gradle.pnpm.interop

import org.gradle.kotlin.dsl.assign
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

/**
 * Copies the TypeScript files
 */
abstract class CopyTypeScriptFiles : DefaultTask() {

  @get:OutputDirectory
  abstract val sourceDirectoryFromDeps: DirectoryProperty

  @get:OutputDirectory
  abstract val sourceDirectoryGenerated: DirectoryProperty

  @get:OutputDirectory
  abstract val sourceDirectoryPredefined: DirectoryProperty

  /**
   * The target directory where the JS module files are copied to
   */
  @get:OutputDirectory
  abstract val targetDirectory: DirectoryProperty

  @TaskAction
  fun copy() {
    project.copy {
      from(sourceDirectoryFromDeps)
      from(sourceDirectoryGenerated)
      from(sourceDirectoryPredefined)
      include("*.ts")
      into(targetDirectory)

      duplicatesStrategy = DuplicatesStrategy.FAIL

      //flatten the directory structure
      eachFile {
        path = name
      }
    }
  }
}
