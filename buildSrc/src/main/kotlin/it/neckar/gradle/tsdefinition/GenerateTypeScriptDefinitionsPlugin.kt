package it.neckar.gradle.tsdefinition

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.task
import withTask
import java.nio.charset.StandardCharsets

/**
 * Plugin that adds a task to create type script definitions from Kotlin source code
 */
private const val EXTENSION_NAME = "createTypeScriptDefinitionsExtension"
/**
 * The name of the task
 */
private const val TASK_NAME = "createTypeScriptDefinitions"

class GenerateTypeScriptDefinitionsPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val extension = project.extensions.create<GenerateTypeScriptDefinitionsExtension>(EXTENSION_NAME).apply {
      //Sets the default configuration
      typeScriptDefinitionFile.convention {
        project.buildDir.resolve("index.d.ts")
      }
    }

    val task = project.task<GenerateTypeScriptDefinitionsTask>(TASK_NAME) {
      group = "Build"
      description = "Creates TypeScript definitions"

      //connect the properties
      namespace.set(extension.namespace)
      typeScriptDefinitionFile.set(extension.typeScriptDefinitionFile)
      exportedFiles.set(extension.export)
      exportConfigFile.set(extension.exportConfigFile)
    }

    //Execute before assemble
    project.withTask("assemble") {
      it.dependsOn(task)
    }
  }
}

/**
 * The extension
 */
open class GenerateTypeScriptDefinitionsExtension(objects: ObjectFactory) {
  /**
   * The namespace
   */
  @Input
  var namespace: Property<String> = objects.property()

  /**
   * The file name for the typescript definition (usually `index.d.ts`)
   */
  @OutputFile
  var typeScriptDefinitionFile: RegularFileProperty = objects.fileProperty()

  /**
   * The Kotlin file that is exported
   */
  @Optional
  @InputFiles
  val export: Property<FileCollection> = objects.property(FileCollection::class.java)

  /**
   * Reference to the export paths configuration file (usually `ts-generation.paths`)
   */
  @Optional
  @InputFile
  val exportConfigFile: RegularFileProperty = objects.fileProperty()
}

/**
 * The task itself
 */
open class GenerateTypeScriptDefinitionsTask : DefaultTask() {
  /**
   * The namespace
   */
  @Input
  val namespace: Property<String> = project.objects.property()

  /**
   * The files that are exported
   */
  @Optional
  @InputFiles
  val exportedFiles: Property<FileCollection> = project.objects.property()

  @Optional
  @InputFile
  val exportConfigFile: RegularFileProperty = project.objects.fileProperty()

  @OutputFile
  val typeScriptDefinitionFile: RegularFileProperty = project.objects.fileProperty()

  @TaskAction
  fun processTemplates() {
    val targetFile = typeScriptDefinitionFile.get().asFile
    targetFile.parentFile.mkdirs()

    val filesToExport = collectFilesToExport()

    val kotlinSourceFileContents = filesToExport
      .map { sourceFile ->
        logger.debug("Loading ${sourceFile.absolutePath}")

        sourceFile.inputStream().use { input ->
          input.readBytes().toString(StandardCharsets.UTF_8)
        }
      }

    val typeScriptDefinitionContent = TypeScriptDefinitionCreator(namespace.getOrElse(project.group.toString())).create(kotlinSourceFileContents)

    logger.debug("Writing ${targetFile.absolutePath}")
    targetFile.outputStream().use { output ->
      output.writer().use {
        it.write(typeScriptDefinitionContent)
      }
    }
  }

  private fun collectFilesToExport(): FileCollection {
    val exportConfigFileAsFile = exportConfigFile.orNull ?: return exportedFiles.get()

    val lines = exportConfigFileAsFile.asFile.readLines().filter {
      it.isNotBlank()
    }

    return project.files(lines)
  }
}

/**
 * Configure the type script definitions plugin
 */
fun Project.generateTypeScriptDefinitions(configure: GenerateTypeScriptDefinitionsExtension.() -> Unit): Unit =
  (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(EXTENSION_NAME, configure)

