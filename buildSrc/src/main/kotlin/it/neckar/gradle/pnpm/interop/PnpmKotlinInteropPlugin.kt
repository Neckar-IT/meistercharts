package it.neckar.gradle.pnpm.interop

import ConfiguredProject
import Projects
import it.neckar.gradle.ksp.generating.ts.CollectTypescriptDeclarationFilesFromDependenciesTask
import it.neckar.gradle.ksp.generating.ts.ProvidePredefinedTypeScriptFilesTask
import it.neckar.gradle.ksp.generating.ts.ProvideTypescriptDeclarationFilesTask
import it.neckar.gradle.ksp.generating.ts.TypescriptDefinitionGenerationPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

/**
 * Configures a pnpm project to be able to directly use Kotlin code from another project.
 */
class PnpmKotlinInteropPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create<PnpmKotlinInteropExtension>(ExtensionName).also {
      it.targetDirectory.convention(target.layout.buildDirectory.dir("generated"))
    }

    val kotlinProjectProvider = target.provider {
      target.project(extension.kotlinProjectPath.get())
    }

    val copyJsModuleFilesTask = target.tasks.register<CopyJsModuleFiles>("copyJsModuleFiles") {
      group = "interop"
      description = "Copies the js module files to the target directory"

      dependsOn(kotlinProjectProvider.flatMap { it.tasks.named("jsProductionExecutableCompileSync") })

      sourceDirectory.set(kotlinProjectProvider.flatMap {
        it.layout.buildDirectory.dir("compileSync/js/main/productionExecutable/kotlin")
      })
      targetDirectory.set(extension.targetDirectory)
    }


    val copyTypeScriptFilesTask = target.tasks.register<CopyTypeScriptFiles>("copyTypeScriptFiles") {
      group = "interop"
      description = "Copies the TypeScript files to the target directory"


      val collectTypescriptDeclarationFilesTask = kotlinProjectProvider.flatMap { kotlinProject -> kotlinProject.tasks.named<CollectTypescriptDeclarationFilesFromDependenciesTask>(TypescriptDefinitionGenerationPlugin.CollectTypescriptDeclarationFilesFromDependenciesTaskTaskName) }
      val provideTypescriptDeclarationFileTask = kotlinProjectProvider.flatMap { kotlinProject -> kotlinProject.tasks.named<ProvideTypescriptDeclarationFilesTask>(TypescriptDefinitionGenerationPlugin.ProvideTypescriptDeclarationFileTaskName) }
      val providePredefinedTypeScriptDeclarationFilesTask = kotlinProjectProvider.flatMap { kotlinProject -> kotlinProject.tasks.named<ProvidePredefinedTypeScriptFilesTask>(TypescriptDefinitionGenerationPlugin.ProvidePredefinedTypeScriptDeclarationFilesTaskName) }

      dependsOn(collectTypescriptDeclarationFilesTask, provideTypescriptDeclarationFileTask, providePredefinedTypeScriptDeclarationFilesTask)

      sourceDirectoryFromDeps.set(collectTypescriptDeclarationFilesTask.flatMap { it.targetDirectory })
      sourceDirectoryGenerated.set(provideTypescriptDeclarationFileTask.flatMap { it.targetDirectory })
      sourceDirectoryPredefined.set(providePredefinedTypeScriptDeclarationFilesTask.flatMap { it.targetDirectory })

      targetDirectory.set(extension.targetDirectory)
    }

    target.tasks.named("pnpmRunBuild") {
      dependsOn(copyJsModuleFilesTask, copyTypeScriptFilesTask)
    }
  }

  companion object {
    const val ExtensionName: String = "pnpmKotlinInteropExtension"
  }
}


open class PnpmKotlinInteropExtension(objects: ObjectFactory) {
  /**
   * Path to the Kotlin project that provides the JavaScript module files.
   */
  @Input
  val kotlinProjectPath: Property<String> = objects.property(String::class.java)

  /**
   * Helper method to set the Kotlin project.
   */
  var kotlinProject: ConfiguredProject
    get() {
      return Projects.find(kotlinProjectPath.get())
    }
    set(value) {
      kotlinProjectPath.set(value.path)
    }

  /**
   * The target directory where the files are copied to.
   */
  val targetDirectory: DirectoryProperty = objects.directoryProperty()
}

/**
 * Configure the TypeScript definitions plugin
 */
fun Project.pnpmKotlinInterop(kotlinProject: ConfiguredProject, configure: PnpmKotlinInteropExtension.() -> Unit) {
  (this as ExtensionAware).extensions.configure<PnpmKotlinInteropExtension>(PnpmKotlinInteropPlugin.ExtensionName) {
    this.kotlinProject = kotlinProject
    configure()
  }
}
