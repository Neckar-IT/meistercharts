package it.neckar.gradle.icons

import it.neckar.gradle.baseNames
import it.neckar.gradle.listSvgFilesRecursively
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import it.neckar.gradle.toCamelCase

/**
 * Generates the icons classes
 */
@Suppress("unused")
class GenerateIconsPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create<GenerateIconsExtension>(GenerateIconsExtensionName)

    target.tasks.register<CreateBasicIconsDeclarationTask>(CreateIconDeclarationsTaskName) {
      group = "Build"
      description = "Creates BasicIcons definitions"

      //connect the properties
      svgIconsSourceDir = extension.svgIconsSourceDir
      objectName = extension.objectName

      //Output properties
      basicIconsFile = extension.basicIconsFile
      svgPathsFile = extension.svgPathsFile
      svgPaintableProvidersFile = extension.svgPaintableProvidersFile
    }
  }

  companion object {
    const val GenerateIconsExtensionName: String = "createIcons"
    const val CreateIconDeclarationsTaskName: String = "createIconDeclarations"
  }
}

/**
 * Extension for the generate icons plugin
 */
abstract class GenerateIconsExtension {
  /**
   * The class name of the generated object
   */
  abstract val objectName: Property<String>

  /**
   * The directory where the svg source icons are located
   */
  abstract val svgIconsSourceDir: DirectoryProperty

  /**
   * If set, generates the Basic Icons file that contains the icon IDs
   */
  abstract val basicIconsFile: RegularFileProperty

  /**
   * If set, generates the svg paths object
   */
  abstract val svgPathsFile: RegularFileProperty

  /**
   * If set, generates the svg paintable providers object
   */
  abstract val svgPaintableProvidersFile: RegularFileProperty

  /**
   * The svg paintables provider factory
   */
  abstract val svgPaintablesProviderProviderFile: RegularFileProperty
}

@CacheableTask
abstract class CreateBasicIconsDeclarationTask : DefaultTask() {
  @get:Input
  abstract val objectName: Property<String>

  @get:PathSensitive(PathSensitivity.RELATIVE)
  @get:InputDirectory
  abstract val svgIconsSourceDir: DirectoryProperty

  @get:Optional
  @get:OutputFile
  abstract val basicIconsFile: RegularFileProperty

  @get:Optional
  @get:OutputFile
  abstract val svgPathsFile: RegularFileProperty

  @get:Optional
  @get:OutputFile
  abstract val svgPaintableProvidersFile: RegularFileProperty

  @TaskAction
  fun generate() {
    val svgFiles = (svgIconsSourceDir.asFile.get()).listSvgFilesRecursively()
    val iconBaseNames = svgFiles.baseNames()

    //Create the basic icons file - if configured
    basicIconsFile.orNull?.let {
      logger.info("Creating basic icons file @ $it")
      val objectName = it.asFile.nameWithoutExtension.toCamelCase().capitalize()

      val content = GeneratePaintableObject(iconBaseNames, objectName, guessPackageName(it)).create()
      it.asFile.outputStream().use { output ->
        output.writer().use { out ->
          out.write(content)
        }
      }
    }

    /**
     * Creates the svg paths object - if configured
     */
    svgPathsFile.orNull?.let {
      logger.info("Creating svg paths file @ $it")
      val objectName = it.asFile.nameWithoutExtension.toCamelCase().capitalize()
      val content = GenerateSvgPaths(svgFiles, objectName, guessPackageName(it)).create()
      it.asFile.outputStream().use { output ->
        output.writer().use { out ->
          out.write(content)
        }
      }
    }
    /**
     * Creates the svg paintable providers object - requires the svg paths file
     */
    svgPaintableProvidersFile.orNull?.let {
      logger.info("Creating svg paintables file @ $it")
      val objectName = it.asFile.nameWithoutExtension.toCamelCase().capitalize()
      val content = GenerateSvgPathsProviders(iconBaseNames, objectName, guessPackageName(it)).create()
      it.asFile.outputStream().use { output ->
        output.writer().use { out ->
          out.write(content)
        }
      }
    }
  }

  private fun guessPackageName(it: RegularFile): String {
    val filePath = it.asFile.absolutePath

    val pattern = "commonMain/kotlin/"
    val startIndex = filePath.lastIndexOf(pattern) + pattern.length
    val toIndex = filePath.lastIndexOf("/")

    val packagePart = filePath.substring(startIndex, toIndex)
    return packagePart.replace('/', '.').lowercase()
  }
}

/**
 * Generate icons and related classes
 */
@Suppress("unused")
fun Project.generateIcons(configure: GenerateIconsExtension.() -> Unit): Unit =
  (this as org.gradle.api.plugins.ExtensionAware).extensions.configure(GenerateIconsPlugin.GenerateIconsExtensionName, configure)
