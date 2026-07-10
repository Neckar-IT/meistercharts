@file:Suppress("unused")

package it.neckar.gradle.npmbundle

import it.neckar.gradle.hasKotlinMultiplatformPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import it.neckar.gradle.withTask


/**
 * Creates a NPM module.
 *
 * The bundled package.json is the file checked in next to the Gradle project (see #1635).
 */
class NpmBundlePlugin : Plugin<Project> {
  override fun apply(target: Project) {

    /**
     * The extension that configures the npm bundle (production)
     */
    val npmBundleExtensionProduction = target.extensions.create<NpmBundleExtension>(ExtensionName).apply {
      moduleName.convention(target.name)
      dirNameInArchive.convention(target.name)
      archiveFileName.convention(target.name)

      filesToBundle.convention(
        target.copySpec {
          from(target.layout.projectDirectory.file("package.json"))

          from(target.layout.buildDirectory.file("kotlin-webpack/js/productionExecutable"))
          include("*.js", "*.map", "*.html", "*.d.ts", "*.json")

          from(target.layout.buildDirectory.file("generated/ksp/js/jsMain/resources"))
          include("*.d.ts")
        }
      )

      version.convention(target.version.toString())

      workingDir.convention(target.layout.buildDirectory.dir("npm-work"))
      targetDirectoryForArchive.convention(target.layout.buildDirectory.dir("npm"))
    }

    /**
     * The extension that configures the npm bundle (development)
     */
    val npmBundleExtensionDevelopment = target.extensions.create<NpmBundleExtension>(DevelopmentExtensionName).apply {
      moduleName.convention(target.name)
      dirNameInArchive.convention(target.name)
      archiveFileName.convention(target.name)

      filesToBundle.convention(
        target.copySpec {
          from(target.layout.projectDirectory.file("package.json"))

          from(target.layout.buildDirectory.file("kotlin-webpack/js/developmentExecutable"))
          include("*.js", "*.map", "*.html", "*.d.ts", "*.json")

          from(target.layout.buildDirectory.file("generated/ksp/js/jsMain/resources"))
          include("*.d.ts")

          from(target.layout.buildDirectory.file("generated/ksp/js/jsTest/resources"))
          include("*.d.ts")
        }
      )

      version.convention(target.version.toString())

      workingDir.convention(target.layout.buildDirectory.dir("npmDevelopment-work"))
      targetDirectoryForArchive.convention(target.layout.buildDirectory.dir("npmDevelopment"))
    }

    /**
     * Task that copies the NPM content (production)
     */
    val npmCopyBundleContentTask = target.tasks.register<CopyBundleContentTask>(NpmCopyBundleContentTaskName) {
      group = TaskGroup
      description = "Collects the content to the working directory"

      destinationDir = npmBundleExtensionProduction.workingDir
      filesToBundle = npmBundleExtensionProduction.filesToBundle

      when {
        target.hasKotlinMultiplatformPlugin() -> {
          dependsOn("jsBrowserProductionWebpack")
        }

        else -> {
          throw IllegalStateException("Attention! no Multiplatform plugin found")
        }
      }
    }

    val npmCopyBundleContentDevelopmentTask = target.tasks.register<CopyBundleContentTask>(NpmCopyBundleContentTaskNameDevelopment) {
      group = TaskGroup
      description = "Collects the content to the working directory (Development)"

      destinationDir = npmBundleExtensionDevelopment.workingDir
      filesToBundle = npmBundleExtensionDevelopment.filesToBundle

      //Add the deps automatically for Kotlin projects
      when {
        target.hasKotlinMultiplatformPlugin() -> {
          dependsOn("jsBrowserDevelopmentWebpack")
        }

        else -> {
          throw IllegalStateException("Attention! no Multiplatform plugin found")
        }
      }
    }

    val verifyBundleContentTask = target.tasks.register<VerifyBundleContentTask>(VerifyBundleContentTaskName) {
      group = TaskGroup
      description = "Verifies the content of the bundle"

      dependsOn(npmCopyBundleContentTask)

      workingDir = npmBundleExtensionProduction.workingDir
    }

    val verifyBundleContentTaskDevelopment = target.tasks.register<VerifyBundleContentTask>(VerifyBundleContentDevelopmentTaskName) {
      group = TaskGroup
      description = "Verifies the content of the bundle (Development)"

      dependsOn(npmCopyBundleContentDevelopmentTask)

      workingDir = npmBundleExtensionDevelopment.workingDir
    }

    //Ensure that the verifyBundleContentTask is executed after the npmCopyBundleContentTask
    npmCopyBundleContentTask.configure { finalizedBy(verifyBundleContentTask) }
    npmCopyBundleContentDevelopmentTask.configure { finalizedBy(verifyBundleContentTaskDevelopment) }

    /**
     * Production task
     */
    val gzipTask = target.tasks.register<GzipNpmModuleTask>(NpmBundleTaskName) {
      group = TaskGroup
      description = "Creates the npm bundle (*.tar.gz)"

      dependsOn(npmCopyBundleContentTask)

      targetDirectoryForArchiveProperty = npmBundleExtensionProduction.targetDirectoryForArchive
      sourceDirProperty = npmBundleExtensionProduction.workingDir
      dirNameInArchiveProperty = npmBundleExtensionProduction.dirNameInArchive
      archiveFileNameProperty = npmBundleExtensionProduction.archiveFileName
    }

    /**
     * Development task
     */
    target.tasks.register<GzipNpmModuleTask>(NpmBundleDevelopmentTaskName) {
      group = TaskGroup
      description = "Creates the npm bundle (*.tar.gz) - (Development)"

      dependsOn(npmCopyBundleContentDevelopmentTask)

      targetDirectoryForArchiveProperty = npmBundleExtensionDevelopment.targetDirectoryForArchive
      sourceDirProperty = npmBundleExtensionDevelopment.workingDir
      dirNameInArchiveProperty = npmBundleExtensionDevelopment.dirNameInArchive
      archiveFileNameProperty = npmBundleExtensionDevelopment.archiveFileName
    }

    //Execute before assemble
    target.withTask("assemble") {
      it.dependsOn(gzipTask)
    }
  }

  companion object {
    const val TaskGroup: String = "Neckar IT - NPM Bundle"

    const val ExtensionName: String = "npmBundle"
    const val DevelopmentExtensionName: String = "npmBundleDevelopment"

    const val NpmCopyBundleContentTaskName: String = "npmCopyBundleContent"
    const val NpmCopyBundleContentTaskNameDevelopment: String = "npmCopyBundleContentDevelopment"

    const val NpmBundleTaskName: String = "npmBundle"
    const val NpmBundleDevelopmentTaskName: String = "npmBundleDevelopment"

    const val VerifyBundleContentTaskName: String = "verifyBundleContent"
    const val VerifyBundleContentDevelopmentTaskName: String = "verifyBundleContentDevelopment"
  }
}

