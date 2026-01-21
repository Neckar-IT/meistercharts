package it.neckar.gradle.pnpm.dependencies

import org.gradle.kotlin.dsl.assign
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

open class InstallPnpmDependencyToNpmDependenciesProject : BasePnpmDependencyTask() {
  init {
    description = "Installs an npm dependency to this `npm-dependencies-project`"

    outputs.upToDateWhen { false }
    //finalizedBy(":kotlinUpgradeYarnLock")
    //finalizedBy(":internal:closed:tools:npm-dependencies-project:build")
    //finalizedBy("pnpmInstall")
    finalizedBy(":kotlinUpgradeYarnLock")
  }

  @OutputFile
  val dependencyFile: RegularFileProperty = project.objects.fileProperty()

  @TaskAction
  fun installDependency() {
    /**
     * Hard-coded check to ensure this task is only called for the `npm-dependencies-project`
     */
    require(project.name == "npm-dependencies-project") {
      "This task is only relevant for the `npm-dependencies-project`"
    }

    val dependency = getDependencyParameter()

    logger.lifecycle("Installing npm dependency: $dependency")

    //add the dependency to the deps.json file
    val dependencyFile = this.dependencyFile.get().asFile

    val dependencyFileContent = dependencyFile.readText()


    val parsed = json.parseToJsonElement(dependencyFileContent)
    val currentEntries = parsed.jsonArray.map { it.jsonPrimitive.content }.toSet()
    val newEntries = (currentEntries + dependency).toList().sorted()

    val updated = json.encodeToString(
      serializer = JsonArray.serializer(),
      value = JsonArray(newEntries.map { JsonPrimitive(it) })
    )

    //Write the updated file
    dependencyFile.writeText(updated)
  }
}
