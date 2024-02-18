package it.neckar.gradle.kps.generating.ts

import Plugins
import Projects
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.property
import toUpperCamelCase
import project
/**
 * Plugin that adds a task to create type script definitions from Kotlin source code
 */
private const val EXTENSION_NAME = "createTypeScriptDefinitionsExtension"

/**
 * Registers the dependency to the TypeScript Definition Generator
 */
class TypescriptDefinitionGenerationPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create<GenerateTypeScriptDefinitionsExtension>(EXTENSION_NAME).apply {
      //Sets the default configuration
      targetTypescriptDefinitionFileName.convention("index.d.ts")
      targetTypescriptFileName.convention("index.ts")
      annotationName.convention("kotlin.js.JsExport")
      namespace.convention(target.name.toUpperCamelCase())
    }

    //Apply the KSP plugin
    target.plugins.apply(Plugins.ksp)

    val kspConfigurations = target.configurations
      .filter { it.name == "kspJs" }

    if (kspConfigurations.isEmpty()) {
      throw IllegalStateException("No [kspJs] configuration found for ${target.name}.")
    }

    kspConfigurations
      .forEach {
        val dependencies = target.dependencies
        dependencies.add(it.name, target.project(Projects.open_ksp_generating_ts_declaration))
      }

    target.extensions.findByType<com.google.devtools.ksp.gradle.KspExtension>()?.let { kspExtension ->
      //kspExtension.arg("kotlinGenerateDts", extension.exportConfigFile.orNull?.asFile?.absolutePath ?: "not set")
      kspExtension.arg {
        val annotationName = extension.annotationName.get()
        val typeScriptDefinitionFileName = extension.targetTypescriptDefinitionFileName.get()
        val targetTypescriptFileName = extension.targetTypescriptFileName.get()
        val namespace = extension.namespace.get()

        listOf(
          "annotationName=$annotationName",
          "typeScriptDefinitionFileName=$typeScriptDefinitionFileName",
          "typeScriptFileName=$targetTypescriptFileName",
          "namespace=$namespace",
        )
      }
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
  val namespace: Property<String> = objects.property()

  /**
   * The file name for the typescript definition (usually `index.d.ts`)
   */
  @OutputFile
  val targetTypescriptDefinitionFileName: Property<String> = objects.property()

  /**
   * The name of the file containing the constants (usually `index.ts`)
   */
  @OutputFile
  val targetTypescriptFileName: Property<String> = objects.property()

  /**
   * The annotation name. Default is "kotlin.js.JsExport")
   */
  @Input
  val annotationName: Property<String> = objects.property()
}


/**
 * Configure the type script definitions plugin
 */
fun Project.generateTypeScriptDefinitions(configure: GenerateTypeScriptDefinitionsExtension.() -> Unit): Unit =
  (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(EXTENSION_NAME, configure)

