@file:Suppress("unused")

package it.neckar.gradle.npmbundle

import hasKotlinMultiplatformPlugin
import it.neckar.gradle.pnpm.packagejson.GeneratePackageJsonPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.task
import withTask


/**
 * Creates a NPM module.
 *
 * Most of the time the package.json is created using the [it.neckar.gradle.pnpm.packagejson.GeneratePackageJsonPlugin].
 */
class NpmBundlePlugin : Plugin<Project> {
  override fun apply(project: Project) {

    /**
     * The extension that configures the npm bundle (production)
     */
    val npmBundleExtensionProduction = project.extensions.create<NpmBundleExtension>(NpmBundleExtensionName).apply {
      moduleName.convention(project.name)
      dirNameInArchive.convention(project.name)
      archiveFileName.convention(project.name)

      filesToBundle.convention(
        project.copySpec {
          from(project.layout.projectDirectory.file("package.json"))

          from(project.layout.buildDirectory.file("kotlin-webpack/js/productionExecutable"))
          include("*.js", "*.map", "*.html", "*.d.ts", "*.json")

          from(project.layout.buildDirectory.file("generated/ksp/js/jsMain/resources"))
          include("*.d.ts")
        }
      )

      version.convention(project.version.toString())

      workingDir.convention(project.layout.buildDirectory.dir("npm-work"))
      targetDirectoryForArchive.convention(project.layout.buildDirectory.dir("npm"))
    }

    /**
     * The extension that configures the npm bundle (development)
     */
    val npmBundleExtensionDevelopment = project.extensions.create<NpmBundleExtension>(NpmBundleDevelopmentExtensionName).apply {
      moduleName.convention(project.name)
      dirNameInArchive.convention(project.name)
      archiveFileName.convention(project.name)

      filesToBundle.convention(
        project.copySpec {
          from(project.layout.projectDirectory.file("package.json"))

          from(project.layout.buildDirectory.file("kotlin-webpack/js/developmentExecutable"))
          include("*.js", "*.map", "*.html", "*.d.ts", "*.json")

          from(project.layout.buildDirectory.file("generated/ksp/js/jsMain/resources"))
          include("*.d.ts")

          from(project.layout.buildDirectory.file("generated/ksp/js/jsTest/resources"))
          include("*.d.ts")
        }
      )

      version.convention(project.version.toString())

      workingDir.convention(project.layout.buildDirectory.dir("npmDevelopment-work"))
      targetDirectoryForArchive.convention(project.layout.buildDirectory.dir("npmDevelopment"))
    }

    /**
     * Task that copies the NPM content (production)
     */
    val npmCopyBundleContentTask = project.task<CopyBundleContentTask>(NpmCopyBundleContentTaskName) {
      group = Group
      description = "Collects the content to the working directory"

      destinationDir = npmBundleExtensionProduction.workingDir
      filesToBundle = npmBundleExtensionProduction.filesToBundle

      //Well-known dependency
      project.tasks.findByName(GeneratePackageJsonPlugin.GeneratePackageJsonTaskName)?.let {
        dependsOn(it)
      }

      when {
        project.hasKotlinMultiplatformPlugin() -> {
          dependsOn("jsBrowserProductionWebpack")
        }

        else -> {
          throw IllegalStateException("Attention! no Multiplatform plugin found")
        }
      }
    }

    val npmCopyBundleContentDevelopmentTask = project.task<CopyBundleContentTask>(NpmCopyBundleContentTaskNameDevelopment) {
      group = Group
      description = "Collects the content to the working directory (Development)"

      destinationDir = npmBundleExtensionDevelopment.workingDir
      filesToBundle = npmBundleExtensionDevelopment.filesToBundle

      //TODO add task dependencies

      //Well-known dependency
      project.tasks.findByName(GeneratePackageJsonPlugin.GeneratePackageJsonTaskName)?.let {
        dependsOn(it)
      }

      //Add the deps automatically for Kotlin projects
      when {
        project.hasKotlinMultiplatformPlugin() -> {
          dependsOn("jsBrowserDevelopmentWebpack")
        }

        else -> {
          throw IllegalStateException("Attention! no Multiplatform plugin found")
        }
      }
    }

    val verifyBundleContentTask = project.task<VerifyBundleContentTask>(VerifyBundleContentTaskName) {
      group = Group
      description = "Verifies the content of the bundle"

      dependsOn(npmCopyBundleContentTask)

      workingDir = npmBundleExtensionProduction.workingDir
    }

    val verifyBundleContentTaskDevelopment = project.task<VerifyBundleContentTask>(VerifyBundleContentDevelopmentTaskName) {
      group = Group
      description = "Verifies the content of the bundle (Development)"

      dependsOn(npmCopyBundleContentDevelopmentTask)

      workingDir = npmBundleExtensionDevelopment.workingDir
    }

    //Ensure that the verifyBundleContentTask is executed after the npmCopyBundleContentTask
    npmCopyBundleContentTask.finalizedBy(verifyBundleContentTask)
    npmCopyBundleContentDevelopmentTask.finalizedBy(verifyBundleContentTaskDevelopment)

    /**
     * Production task
     */
    val gzipTask = project.task<GzipNpmModuleTask>(NpmBundleTaskName) {
      group = Group
      description = "Creates the npm bundle (*.tar.gz)"

      dependsOn(npmCopyBundleContentTask, GeneratePackageJsonPlugin.GeneratePackageJsonTaskName) //TODO remove!

      targetDirectoryForArchiveProperty = npmBundleExtensionProduction.targetDirectoryForArchive
      sourceDirProperty = npmBundleExtensionProduction.workingDir
      dirNameInArchiveProperty = npmBundleExtensionProduction.dirNameInArchive
      archiveFileNameProperty = npmBundleExtensionProduction.archiveFileName
    }

    /**
     * Development task
     */
    project.task<GzipNpmModuleTask>(NpmBundleDevelopmentTaskName) {
      group = Group
      description = "Creates the npm bundle (*.tar.gz) - (Development)"

      //dependsOn(npmBundleContentDevelopmentTask, packageJsonTaskDevelopment)
      dependsOn(npmCopyBundleContentDevelopmentTask, GeneratePackageJsonPlugin.GeneratePackageJsonTaskName)

      targetDirectoryForArchiveProperty = npmBundleExtensionDevelopment.targetDirectoryForArchive
      sourceDirProperty = npmBundleExtensionDevelopment.workingDir
      dirNameInArchiveProperty = npmBundleExtensionDevelopment.dirNameInArchive
      archiveFileNameProperty = npmBundleExtensionDevelopment.archiveFileName
    }

    //Execute before assemble
    project.withTask("assemble") {
      it.dependsOn(gzipTask)
    }
  }

  companion object {
    const val Group: String = "Neckar IT - NPM Bundle"

    const val NpmBundleExtensionName: String = "npmBundle"
    const val NpmBundleDevelopmentExtensionName: String = "npmBundleDevelopment"

    const val NpmCopyBundleContentTaskName: String = "npmCopyBundleContent"
    const val NpmCopyBundleContentTaskNameDevelopment: String = "npmCopyBundleContentDevelopment"

    const val NpmBundleTaskName: String = "npmBundle"
    const val NpmBundleDevelopmentTaskName: String = "npmBundleDevelopment"

    const val VerifyBundleContentTaskName: String = "verifyBundleContent"
    const val VerifyBundleContentDevelopmentTaskName: String = "verifyBundleContentDevelopment"
  }
}

