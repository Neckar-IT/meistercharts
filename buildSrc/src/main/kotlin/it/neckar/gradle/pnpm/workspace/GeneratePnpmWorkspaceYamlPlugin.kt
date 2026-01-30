package it.neckar.gradle.pnpm.workspace

import Plugins
import Projects
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.task


/**
 * Generates the workspace.yaml file
 */
class GeneratePnpmWorkspaceYamlPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.base) //Ensure that the base plugin is applied

    val extension = target.extensions.create<GenerateWorkspaceYamlPluginExtension>("generatePnpmWorkspaceYaml").apply {
      targetFile.convention(target.layout.projectDirectory.file("pnpm-workspace.yaml"))
    }

    val generateWorkspaceYamlTask = target.task<GenerateWorkspaceYamlTask>(GenerateWorkspaceYamlTaskName) {
      targetFile = extension.targetFile
      manualEntries = extension.manualEntries
    }

    //Update the clean task to delete the generated file
    target.tasks.named<Delete>("clean") {
      delete(generateWorkspaceYamlTask.targetFile)
    }
  }

  companion object {
    const val GenerateWorkspaceYamlTaskName: String = "generateWorkspaceYaml"
  }
}

open class GenerateWorkspaceYamlPluginExtension(objects: ObjectFactory) {
  /**
   * Where to write the generated workspace.yaml file
   */
  val targetFile: RegularFileProperty = objects.fileProperty()

  /**
   * Contains additional entries that will be added to the workspace.yaml file
   */
  val manualEntries: ListProperty<String> = objects.listProperty(String::class)
}

abstract class GenerateWorkspaceYamlTask : DefaultTask() {
  init {
    group = "build"
    description = "Generates a workspace.yaml file"
  }

  @get:OutputFile
  abstract val targetFile: RegularFileProperty

  /**
   * Contains additional entries that will be added to the workspace.yaml file
   */
  @get:Input
  abstract val manualEntries: ListProperty<String>

  @TaskAction
  fun generate() {
    val targetFile = targetFile.get().asFile

    val pnpmProjectPaths = Projects.pnpmProjects()
      .map { it.path.path.replace(":", "/") }
      .map {
        it.removePrefix("/") //remove leading slash
      }
      .sorted()

    val content = buildString {
      appendLine("packages:")
      appendLine("  # Automatically generated from the projects defined in Projects.kt")
      pnpmProjectPaths.forEach { path ->
        appendLine("  - $path")
      }

      appendLine("")
      appendLine("  # Manual entries - as configured in build.gradle.kts")
      manualEntries.get().forEach { entry ->
        appendLine("  - $entry")
      }
    }

    targetFile.writeText(content)
  }
}

