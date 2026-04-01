package it.neckar.gradle.pnpm.packagejson

import Plugins
import normalizeNpmNameToAlias
import npmVersion
import isSymLinkTo
import it.neckar.gradle.ansiConsole
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.task


/**
 * Generates a package.json file from a template.
 *
 * The package.json is a symlink to the generated package.json file.
 * The package.json file is always checked in to ensure the project is recognizable as a node project.
 *
 * If the generated package.json does not exist, the project will fail to build (instead of being ignored)
 */
class GeneratePackageJsonPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.plugins.apply(Plugins.base) //Ensure that the base plugin is applied

    val extension = target.extensions.create<GeneratePackageJsonPluginExtension>("generatePackageJson").apply {
      npmVersionsToml.convention {
        target.rootProject.file("gradle/npm.versions.toml")
      }

      templateFile.convention {
        target.file("package.template.json")
      }

      targetFile.convention(target.layout.projectDirectory.file(PackageGeneratedJsonFileName)) //generate the package.json in the *same* directory to avoid problems with relative paths (e.g., pnpm)
      packageJsonSymLinkFile.convention(target.layout.projectDirectory.file("package.json"))

      moduleName = target.name
      version.convention(target.version.toString())
    }

    val generatePackageJsonTask = target.task<GeneratePackageJsonTask>(GeneratePackageJsonTaskName) {
      npmVersionsToml = extension.npmVersionsToml
      templateFile = extension.templateFile
      targetFile = extension.targetFile
      packageJsonSymLinkFile = extension.packageJsonSymLinkFile
      moduleName = extension.moduleName
      version = extension.version
    }

    //Delete generated file on clean
    target.tasks.named<Delete>("clean") {
      delete(generatePackageJsonTask.targetFile)
    }
  }

  companion object {
    const val GeneratePackageJsonTaskName: String = "generatePackageJson"

    private const val PackageGeneratedJsonFileName = "package.generated.json"
  }
}

open class GeneratePackageJsonPluginExtension(objects: ObjectFactory) {
  /**
   * The `gradle/npm.versions.toml` file that is used to resolve version numbers.
   * Referenced as an input to trigger regeneration when versions change.
   */
  val npmVersionsToml: RegularFileProperty = objects.fileProperty()

  /**
   * The package.template.json file that is read and converted to a package.json file
   */
  val templateFile: RegularFileProperty = objects.fileProperty()

  /**
   * Where to write the generated package.json file
   */
  val targetFile: RegularFileProperty = objects.fileProperty()

  /**
   * Contains a symlink to the generated package.json file
   */
  val packageJsonSymLinkFile: RegularFileProperty = objects.fileProperty()

  /**
   * The module name - will be used as name for the package.json
   */
  val moduleName: Property<String> = objects.property()

  /**
   * The version number - will be used when processing the package.json template
   */
  val version: Property<String> = objects.property()

}

abstract class GeneratePackageJsonTask : DefaultTask() {
  init {
    group = "build"
    description = "Generates a package.json file from a template"
  }

  @get:InputFile
  abstract val templateFile: RegularFileProperty

  @get:OutputFile
  abstract val targetFile: RegularFileProperty

  /**
   * Contains a symlink to the generated package.json file.
   *
   * The symlink is necessary to ensure the package.json exists all the time.
   */
  @get:OutputFile
  abstract val packageJsonSymLinkFile: RegularFileProperty

  @get:Input
  abstract val moduleName: Property<String>

  /**
   * The version number - will be used when processing the package.json template
   */
  @get:Input
  abstract val version: Property<String>

  /**
   * Reference to the `gradle/npm.versions.toml` file.
   * If the file is updated, the package.json file will be regenerated.
   */
  @get:InputFile
  abstract val npmVersionsToml: RegularFileProperty

  @TaskAction
  fun generate() {
    val templateFile = templateFile.get().asFile
    if (templateFile.isFile.not() || templateFile.exists().not()) {
      throw InvalidUserDataException("package.json template not found @ <${templateFile.absolutePath}>")
    }

    val targetFile = targetFile.get().asFile
    val templateContent = templateFile.readText()

    targetFile.writeText(replaceContent(templateContent))

    //Create symlink file
    val symLinkFile = packageJsonSymLinkFile.get().asFile
    require(symLinkFile.isSymLinkTo(targetFile)) {
      "The symlink file <${symLinkFile.absolutePath}> is not a symlink to <${targetFile.absolutePath}>.\n" +
        "Generate the symlink file with:\n" +
        ansiConsole.green("ln -s ${targetFile.name} ${symLinkFile.name}")
    }
  }

  private fun replaceContent(content: String): String {
    //Replace the "version" and module name variables in the package.json template
    val replaced = content
      .replace("\$VERSION", version.get())
      .replace("\$MODULE", moduleName.get())

    //find all version variables in the style ${version.npm.*} and replace them with the resolved version number
    val versionVariableNames = "\\$\\{([^}]+)}".toRegex().findAll(content)
      .map { it.groupValues[1] } //get the first group
      .filter {
        it.startsWith("version.npm.")
      }

    val replacedVersionNumbers = versionVariableNames.fold(replaced) { acc, variableName ->
      val npmPackageName = variableName.removePrefix("version.npm.")
      val alias = normalizeNpmNameToAlias(npmPackageName)
      val versionValue = project.npmVersion(alias)
      acc.replace("\${$variableName}", versionValue)
    }

    return replacedVersionNumbers
  }
}

