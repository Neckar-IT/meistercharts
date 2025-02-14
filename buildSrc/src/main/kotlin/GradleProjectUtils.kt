import com.google.common.io.Files
import it.neckar.docker.ExternalDockerImages
import it.neckar.docker.externalDockerImages
import it.neckar.gradle.ansiConsole
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.tasks.AbstractCopyTask
import java.io.File


/**
 *
 */

//White list of all deps that provide annotations
val annotations: Set<String> = setOf("annotations", "jsr305", "unit")

/**
 * Returns true if the given identifier describes a annotation dependency
 */
fun ComponentArtifactIdentifier.isAnnotationDependency(): Boolean {
  return annotations.asSequence()
    .any { toSkipPart ->
      displayName.contains(toSkipPart)
    }
}

/**
 * Converts a project path to a file name with the given suffix
 */
fun String.projectPath2FileName(suffix: String = ""): String {
  return "${replace(":internal", "").replace(":", ".").trim('.')}$suffix.jar"
}

/**
 * Converts a project path to a file name with the given suffix. Adds "cedarsoft." to the string.
 */
@Deprecated("Needs update related to cedarsoft in path")
fun String.projectPath2FileNameWithCedarsoft(suffix: String = ""): String {
  return projectPath2FileName(suffix).replace("closed", "cedarsoft").replace("open.", "cedarsoft.open.")
}

/**
 * Returns the (first) sources file (if there is one)
 */
fun Project.findSourcesJar(sourcesJarTaskName: String = "sourcesJar"): File? {
  return tasks.findByPath(sourcesJarTaskName)?.outputs?.files?.files?.firstOrNull()
}

/**
 * Copies the sources jar of the given project to the given target dir
 */
@Deprecated("Needs update related to cedarsoft in path")
fun Project.copySourcesJarToDir(
  targetDir: File,
  sourcesJarTaskName: String = "jvmSourcesJar",
) {
  findSourcesJar(sourcesJarTaskName)?.let { sourcesJar ->
    sourcesJar.ensureExists("sourcesJar")
    val targetFile = File(targetDir, path.projectPath2FileNameWithCedarsoft("-sources"))
    Files.copy(sourcesJar, targetFile)
  }
}


fun File.ensureExists(taskNameToCreate: String) {
  if (exists().not()) {
    throw GradleException("File <${this.absolutePath}> does not exist. Run `gradle $taskNameToCreate` for all projects before")
  }
}

/**
 * Converts a component identifier to a file name.
 * This method can be used when copying dependencies
 */
@Deprecated("Needs update related to cedarsoft in path")
fun ComponentIdentifier.toFileName(suffix: String = ""): String {
  return when (val identifier = this) {
    is ProjectComponentIdentifier -> identifier.projectPath.projectPath2FileNameWithCedarsoft(suffix)
    is ModuleComponentIdentifier -> "${identifier.group}.${identifier.module}$suffix.jar"
    else -> throw IllegalArgumentException("identifier invalid $identifier::class")
  }
}

/**
 * Returns true if this project is a kotlin multiplatform project.
 */
fun Project.hasKotlinMultiplatformPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinMultiPlatform)
}

/**
 * Returns true if this project is a kotlin jvm project.
 */
fun Project.hasKotlinJvmPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinJvm)
}

fun Project.hasPlugin(kotlinMultiPlatform: String): Boolean {
  return this.pluginManager.findPlugin(kotlinMultiPlatform) != null
}

/**
 * Returns all project dependencies (including transitive dependencies)
 * for the configurations with the given names.
 */
fun Project.findAllProjectDependencies(
  configurationNames: List<String>,
  foundProjects: MutableSet<Project> = mutableSetOf(),
  visitedProjects: MutableSet<Project> = mutableSetOf(),
): Set<Project> {
  if (this in visitedProjects) return foundProjects
  visitedProjects.add(this)

  configurationNames.forEach { configurationName ->
    configurations.findByName(configurationName)?.let { configuration ->
      val directDependencies = configuration.findDirectProjectDependencies()
      foundProjects.addAll(directDependencies)

      directDependencies.forEach { project ->
        foundProjects.addAll(project.findAllProjectDependencies(configurationNames, foundProjects, visitedProjects))
      }
    }
  }

  return foundProjects
}

/**
 * Returns all project dependencies for the configuration
 */
fun Configuration.findDirectProjectDependencies(): List<Project> {
  return allDependencies
    .filterIsInstance<ProjectDependency>()
    .map { it.dependencyProject }
}

/**
 * Returns the named object or null
 */
fun <T> NamedDomainObjectSet<T>.findNamed(name: String): NamedDomainObjectProvider<T>? {
  return try {
    this.named(name)
  } catch (_: UnknownDomainObjectException) {
    null
  }
}

/**
 * Returns the parsed package.json file
 * Throws an exception if the file does not exist
 */
fun Project.parsePackageJson(): JsonElement {
  val packageJson = file("package.json")

  if (packageJson.isFile.not()) {
    throw GradleException("Expected ${packageJson.absolutePath} to be a file")
  }

  return Json.parseToJsonElement(packageJson.readText())
}

/**
 * Returns true if the package.json file contains the given script
 */
fun Project.packageJsonContainsScript(scriptName: String): Boolean {
  return (parsePackageJson().jsonObject["scripts"]?.jsonObject?.containsKey(scriptName) == true)
}

/**
 * Executes the task only if the property [propertyName] is set to true
 */
fun Task.onlyIfPropertyTrue(propertyName: String) {
  onlyIf {
    val executeTask = project.findProperty(propertyName)?.toString() == "true"
    if (executeTask.not()) {
      logger.lifecycle("${ansiConsole.orange("Skipping $name")} because property ${ansiConsole.green(propertyName)} is not set. Call ${ansiConsole.green("gradle build -P${propertyName}=true")} to execute this task")
    }
    executeTask
  }
}

/**
 * Filters all (external) docker image variables
 */
fun AbstractCopyTask.filterAllExternalDockerImages() {
  val dockerImageTags = it.neckar.docker.ExternalDockerImageTags.loadFromDockerVersionProperties()
  val withTag = ExternalDockerImages.withTag(dockerImageTags)

  filter { line ->
    var currentLine = line

    withTag.forEach { dockerImageDescriptor ->
      val variableName = ExternalDockerImages.variableName(dockerImageDescriptor)
      val oldValue = "\${${variableName}}"
      currentLine = currentLine.replace(oldValue, dockerImageDescriptor.fqName)
    }

    currentLine
  }
}

/**
 * Replaces the variables for external docker images - if available.
 *
 * This method configures everything necessary for filtering.
 * No necessity to call any other methods.
 */
fun AbstractCopyTask.filterExternalDockerImages() {
  //Adds the versions for external docker images as input
  inputs.externalDockerImages()

  doFirst {
    filterAllExternalDockerImages()
  }
}
