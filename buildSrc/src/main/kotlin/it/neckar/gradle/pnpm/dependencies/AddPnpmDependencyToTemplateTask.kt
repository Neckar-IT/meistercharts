package it.neckar.gradle.pnpm.dependencies

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@Suppress("IdentifierGrammar")
abstract class AddPnpmDependencyToTemplateTask : BasePnpmDependencyTask() {
  init {
    description = "Adds an npm dependency to the package.template.json file; synopsis: `gradle addPnpmDependencyToTemplate -P$ArgumentNameDependency={NODE_DEP}`"

    outputs.upToDateWhen { false }
  }

  /**
   * The dependency type to install
   */
  @get:Input
  var npmDependencyType: NpmDependencyType = NpmDependencyType.Production

  @get:OutputFile
  abstract val packageJsonTemplateFile: RegularFileProperty

  @TaskAction
  fun installDependency() {
    val templateFile = this.packageJsonTemplateFile.get().asFile

    val originalJson = json.parseToJsonElement(templateFile.readText())

    val dependency = getDependencyParameter()

    val dependenciesPropertyName = calculateDependenciesPropertyName()

    val originalDependencies = originalJson.jsonObject[dependenciesPropertyName]
    val dependencyMap = originalDependencies?.jsonObject?.toMutableMap() ?: mutableMapOf()

    dependencyMap[dependency] = JsonPrimitive("\${version.npm.$dependency}")


    val updatedJson = originalJson.jsonObject.toMutableMap()
    updatedJson[dependenciesPropertyName] = json.encodeToJsonElement(dependencyMap)

    val updatedJsonElement = json.encodeToJsonElement(updatedJson)
    val updatedJsonString = json.encodeToString(updatedJsonElement)

    templateFile.writeText(updatedJsonString)
  }

  private fun calculateDependenciesPropertyName(): String {
    return when (npmDependencyType) {
      NpmDependencyType.Production -> "dependencies"
      NpmDependencyType.Dev -> "devDependencies"
      NpmDependencyType.Peer -> "peerDependencies"
    }
  }
}
