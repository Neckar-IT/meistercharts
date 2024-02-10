@file:Suppress("unused")

package it.neckar.gradle.npmbundle

import child
import com.google.common.io.Files
import de.fayard.refreshVersions.core.versionFor
import hasKotlinMultiplatformPlugin
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.task
import withTask
import java.io.FileOutputStream

/**
 * Creates a NPM module
 */
class NpmBundlePlugin : Plugin<Project> {
  override fun apply(project: Project) {
    /**
     * The extension that configures the npm bundle (production)
     */
    val npmBundleExtensionProduction = project.extensions.create<NpmBundleExtension>("npmBundle").apply {
      moduleName.convention(project.name)
      dirNameInArchive.convention(project.name)
      archiveFileName.convention(project.name)

      if (project.rootProject.extra.has("meisterchartsVersion")) {
        version.convention(project.rootProject.extra.get("meisterchartsVersion") as String)
      } else {
        version.convention(project.version.toString())
      }

      kotlinVersion.convention("_")

      workingDir.convention {
        project.file("build/npm/work")
      }

      packageJsonTemplate.convention {
        project.file("package.template.json")
      }

      targetDirectoryForArchive.convention {
        project.file("build/npm")
      }
    }

    /**
     * The extension that configures the npm bundle (development)
     */
    val npmBundleExtensionDevelopment = project.extensions.create<NpmBundleExtension>("npmBundleDevelopment").apply {
      moduleName.convention(project.name)
      dirNameInArchive.convention(project.name)
      archiveFileName.convention(project.name)

      if (project.rootProject.extra.has("meisterchartsVersion")) {
        version.convention(project.rootProject.extra.get("meisterchartsVersion") as String)
      } else {
        version.convention(project.version.toString())
      }

      kotlinVersion.convention("_")

      workingDir.convention {
        project.file("build/npmDevelopment/work")
      }

      packageJsonTemplate.convention {
        project.file("package.template.json")
      }

      targetDirectoryForArchive.convention {
        project.file("build/npmDevelopment")
      }
    }

    val packageJsonTask = project.task<CreatePackageJsonTask>("npmCreatePackageJson") {
      group = "Neckar IT - NPM Bundle"
      description = "Creates the package.json from a template (Production)"

      //connect the properties
      packageJsonTemplateProperty.set(npmBundleExtensionProduction.packageJsonTemplate)
      targetDirProperty.set(npmBundleExtensionProduction.workingDir)
      versionProperty.set(npmBundleExtensionProduction.version)
      kotlinVersionProperty.set(npmBundleExtensionProduction.kotlinVersion)
      moduleNameProperty.set(npmBundleExtensionProduction.moduleName)
    }

    val packageJsonTaskDevelopment = project.task<CreatePackageJsonTask>("npmCreatePackageJsonDevelopment") {
      group = "Neckar IT - NPM Bundle"
      description = "Creates the package.json from a template (Development)"

      //connect the properties
      packageJsonTemplateProperty.set(npmBundleExtensionDevelopment.packageJsonTemplate)
      targetDirProperty.set(npmBundleExtensionDevelopment.workingDir)
      versionProperty.set(npmBundleExtensionDevelopment.version)
      kotlinVersionProperty.set(npmBundleExtensionDevelopment.kotlinVersion)
      moduleNameProperty.set(npmBundleExtensionDevelopment.moduleName)
    }


    /**
     * Task that copies the NPM content (production)
     */
    val npmBundleContentProductionTask = project.task<CopyBundleContentTask>("npmCopyBundleContent") {
      group = "Neckar IT - NPM Bundle"
      description = "Copies the content to the npm bundle"

      //Add depends on (if available)
      val taskToDependOn = project.tasks.findByName("jar") ?: project.tasks.findByName("jsJar")
      taskToDependOn?.let {
        dependsOn(it)
      }

      into(npmBundleExtensionProduction.workingDir.get().asFile)
    }

    val npmBundleContentDevelopmentTask = project.task<CopyBundleContentTask>("npmCopyBundleContentDevelopment") {
      group = "Neckar IT - NPM Bundle"
      description = "Copies the content to the npm bundle (Development)"


      //Add the deps automatically for Kotlin projects
      when {
        project.hasKotlinMultiplatformPlugin() -> {
          dependsOn("jsBrowserDevelopmentWebpack")
        }

        else -> {
          throw IllegalStateException("Attention! no Multiplatform plugin found") //This may be correct, if necessary remove this statement
        }
      }

      into(npmBundleExtensionDevelopment.workingDir.get().asFile)
    }

    /**
     * Production task
     */
    val gzipTask = project.task<GzipNpmModuleTask>("npmBundle") {
      group = "Neckar IT - NPM Bundle"
      description = "Creates the npm bundle (*.tar.gz)"

      dependsOn(npmBundleContentProductionTask, packageJsonTask)

      targetDirectoryForArchiveProperty.set(npmBundleExtensionProduction.targetDirectoryForArchive)
      sourceDirProperty.set(npmBundleExtensionProduction.workingDir)
      dirNameInArchiveProperty.set(npmBundleExtensionProduction.dirNameInArchive)
      archiveFileNameProperty.set(npmBundleExtensionProduction.archiveFileName)
    }

    /**
     * Development task
     */
    project.task<GzipNpmModuleTask>("npmBundleDevelopment") {
      group = "Neckar IT - NPM Bundle"
      description = "Creates the npm bundle (*.tar.gz) - (Development)"

      dependsOn(npmBundleContentDevelopmentTask, packageJsonTaskDevelopment)

      targetDirectoryForArchiveProperty.set(npmBundleExtensionDevelopment.targetDirectoryForArchive)
      sourceDirProperty.set(npmBundleExtensionDevelopment.workingDir)
      dirNameInArchiveProperty.set(npmBundleExtensionDevelopment.dirNameInArchive)
      archiveFileNameProperty.set(npmBundleExtensionDevelopment.archiveFileName)
    }

    //Execute before assemble
    project.withTask("assemble") {
      it.dependsOn(gzipTask)
    }
  }
}

/**
 * Extension for npm bundle
 */
open class NpmBundleExtension(objects: ObjectFactory) {
  /**
   * The module name - will be used as name for the package.json
   */
  @Input
  val moduleName: Property<String> = objects.property()

  /**
   * The name of the directory within the tar.gz file
   */
  @Input
  val dirNameInArchive: Property<String> = objects.property()

  /**
   * The name of the archive file - *without* suffix
   */
  @Input
  val archiveFileName: Property<String> = objects.property()

  /**
   * The version number - will be used when processing the package.json template
   */
  @Input
  val version: Property<String> = objects.property()

  /**
   * The kotlin version number
   */
  @Input
  @Deprecated("Use versionProperty instead")
  val kotlinVersion: Property<String> = objects.property()

  //
  //Below are values that should not be necessary to configure
  //

  /**
   * The npm module directory (usually build/npm).
   * Also contains the module name
   */
  val workingDir: RegularFileProperty = objects.fileProperty()

  /**
   * The package json template file. Will be copied to [workingDir]
   */
  @InputFile
  val packageJsonTemplate: RegularFileProperty = objects.fileProperty()

  /**
   * The directory that contains the tar.gz that contains the package.json and the content provided by [copyContent]
   */
  @OutputDirectory
  val targetDirectoryForArchive: RegularFileProperty = objects.fileProperty()
}

/**
 * Creates the package json file
 */
@Deprecated("Use packageJsonGenerator plugin instead")
open class CreatePackageJsonTask : DefaultTask() {
  @Input
  val versionProperty: Property<String> = project.objects.property()

  @Deprecated("Use versionProperty instead")
  @Input
  val kotlinVersionProperty: Property<String> = project.objects.property()

  @Input
  val moduleNameProperty: Property<String> = project.objects.property()

  @InputFile
  val packageJsonTemplateProperty: RegularFileProperty = project.objects.fileProperty()

  @OutputDirectory
  val targetDirProperty: RegularFileProperty = project.objects.fileProperty()

  @Suppress("unused")
  @TaskAction
  fun processPackageJson() {
    val version = versionProperty.get()
    val kotlinVersion = kotlinVersionProperty.get()
    val module = moduleNameProperty.get()

    val template = packageJsonTemplateProperty.get().asFile

    if (template.isFile.not() || template.exists().not()) {
      throw InvalidUserDataException("package.json template not found @ <${template.absolutePath}>")
    }

    val content = template.readText()
    var replaced = content
      .replace("\$KOTLIN_VERSION", kotlinVersion)
      .replace("\$VERSION", version)
      .replace("\$MODULE", module)


    //find all variables in the style ${variable}
    val versionVariableNames = "\\$\\{([^}]+)}".toRegex().findAll(content)
      .map { it.groupValues[1] } //get the first group
      .filter {
        it.startsWith("version.")
      }

    replaced = versionVariableNames.fold(replaced) { acc, variableName ->
      val versionValue = project.versionFor(variableName)
      acc.replace("\${$variableName}", versionValue)
    }

    val packageJson = targetDirProperty.get().asFile.child("package.json")
    packageJson.parentFile.let { dir ->
      if (!dir.exists()) {
        dir.mkdirs()
      }
    }
    packageJson.writeText(replaced)
  }
}

/**
 * Copies the bundled content
 */
open class CopyBundleContentTask : Copy() {
}

/**
 * Gzips the npm module
 */
open class GzipNpmModuleTask : DefaultTask() {
  @Input
  val archiveFileNameProperty: Property<String> = project.objects.property()

  @Input
  val dirNameInArchiveProperty: Property<String> = project.objects.property()

  @InputDirectory
  val sourceDirProperty: RegularFileProperty = project.objects.fileProperty()

  @OutputDirectory
  val targetDirectoryForArchiveProperty: RegularFileProperty = project.objects.fileProperty()

  @Suppress("unused")
  @TaskAction
  fun zipContent() {
    val sourceDir = sourceDirProperty.get().asFile
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
      throw InvalidUserDataException("Directory does not exist <${sourceDir.absolutePath}>")
    }

    val dirNameInArchive = dirNameInArchiveProperty.get()
    val tarGzFile = targetDirectoryForArchiveProperty.get().asFile.child("${archiveFileNameProperty.get()}.tar.gz")

    //Zip the content
    FileOutputStream(tarGzFile).use { fileOutputStream ->
      GzipCompressorOutputStream(fileOutputStream)
        .use { gzipOut ->
          TarArchiveOutputStream(gzipOut)
            .use { tarOut ->

              val sourceFiles = sourceDir.listFiles() ?: error("Could not list files in ${sourceDir.absolutePath}")
              sourceFiles.forEach { sourceFile ->
                val archiveEntry = tarOut.createArchiveEntry(sourceFile, "$dirNameInArchive/${sourceFile.name}")
                tarOut.putArchiveEntry(archiveEntry)
                Files.copy(sourceFile, tarOut)
                tarOut.closeArchiveEntry()
              }
            }
        }
    }
  }
}
