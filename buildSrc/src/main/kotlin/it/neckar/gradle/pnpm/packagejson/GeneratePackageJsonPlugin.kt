package it.neckar.gradle.pnpm.packagejson

import Plugins
import normalizeNpmNameToAlias
import npmVersion
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.task


/**
 * Generates a package.json file from a `package.template.json` by substituting
 * `$VERSION`, `$MODULE`, and `${version.npm.*}` placeholders against
 * `gradle/npm.versions.toml`.
 *
 * Deprecated as of #1635: the templating pipeline has been replaced by checked-in
 * `package.json` files updated by Renovate's native npm manager. The plugin is
 * kept on disk for archival reference; its inputs (templates and the TOML) have
 * been deleted, so applying it produces a runtime failure.
 */
@Deprecated(
  "Replaced by checked-in package.json + Renovate npm manager (#1635). " +
    "Templates and gradle/npm.versions.toml have been deleted; applying this plugin will fail at task execution.",
)
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

      targetFile.convention(target.layout.projectDirectory.file(PackageJsonFileName))

      moduleName = target.name
      version.convention(target.version.toString())
    }

    target.task<GeneratePackageJsonTask>(GeneratePackageJsonTaskName) {
      npmVersionsToml = extension.npmVersionsToml
      templateFile = extension.templateFile
      targetFile = extension.targetFile
      moduleName = extension.moduleName
      version = extension.version
    }
  }

  companion object {
    const val GeneratePackageJsonTaskName: String = "generatePackageJson"

    private const val PackageJsonFileName = "package.json"
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

