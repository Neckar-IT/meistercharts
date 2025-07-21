import com.google.common.io.Files
import it.neckar.docker.ExternalDockerImages
import it.neckar.docker.variableName
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
import org.gradle.kotlin.dsl.dependencies
import org.gradle.process.ExecOutput
import java.io.File


/**
 * Returns true, if this is the root project
 */
fun Project.isRootProject(): Boolean {
  return this.path == this.rootProject.path
}

/**
 * Returns the fqName of the given project that can be used for file names (e.g., jar files)
 *
 * Example:
 * - :internal:open:common.kotlin-lang -> neckarIT-internal-open-common-kotlin-lang
 *
 *
 * ATTENTION: The same implementation is used in `it.neckar.ksp.ts.model.TsModuleReference`
 * When changing this implementation, please also change the other implementation accordingly!
 */
fun Project.pathAsBaseFileName(): String {
  val rootProjectName = rootProject.name
  return rootProjectName + "-" + this.path.trimStart(':').replace(':', '-')
}

//Allowed-list of all deps that provide annotations
val annotations: Set<String> = setOf("annotations", "jsr305", "unit")

/**
 * Returns true if the given identifier describes a annotation dependency
 */
fun ComponentArtifactIdentifier.isAnnotationDependency(): Boolean {
  return annotations
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
 * Converts a project path to a file name with the given suffix. Adds "neckar-it." to the string.
 */
fun String.projectPath2FileNameWithNeckarIT(suffix: String = ""): String {
  return projectPath2FileName(suffix).replace("closed", "neckar-it").replace("open.", "neckar-it.open.")
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
fun Project.copySourcesJarToDir(
  targetDir: File,
  sourcesJarTaskName: String = "jvmSourcesJar",
) {
  findSourcesJar(sourcesJarTaskName)?.let { sourcesJar ->
    sourcesJar.ensureExists("sourcesJar")
    val targetFile = File(targetDir, path.projectPath2FileNameWithNeckarIT("-sources"))
    Files.copy(sourcesJar, targetFile)
  }
}

/**
 * Throws an exception if the file does not exist.
 * The error message contains the given task name to create the file.
 */
fun File.ensureExists(taskNameToCreate: String) {
  if (exists().not()) {
    throw GradleException("File <${this.absolutePath}> does not exist. Run `gradle $taskNameToCreate` for all projects before")
  }
}

/**
 * Converts a component identifier to a file name.
 * This method can be used when copying dependencies
 */
fun ComponentIdentifier.toFileName(suffix: String = ""): String {
  return when (val identifier = this) {
    is ProjectComponentIdentifier -> identifier.projectPath.projectPath2FileNameWithNeckarIT(suffix)
    is ModuleComponentIdentifier -> {
      "${identifier.group}.${identifier.module}$suffix-${identifier.version}.jar"
    }
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
 *
 * Does *not* include the project itself.
 */
fun Project.findAllProjectDependencies(
  /**
   * The configuration names to search for
   */
  configurationNames: List<String>,
  /**
   * Contains a list of already visited project paths that will be skipped
   */
  alreadyVisitedProjectPaths: MutableSet<String> = mutableSetOf(),
): Set<Project> {
  //Add the own path to the already visited projects, return if the path was already visited
  if (alreadyVisitedProjectPaths.add(this.path).not()) return emptySet()

  //Find the projects for the given configuration names
  val found = configurationNames.flatMap { configurationName ->
    val configuration = configurations.findByName(configurationName)

    configuration
      ?.findDirectProjectDependencies()
      ?.flatMap { it.findAllProjectDependencies(configurationNames, alreadyVisitedProjectPaths) + it }
      ?: emptyList()
  }

  //Project.equals() is not implemented correctly, it is necessary to filter the projects by path manually
  return found
    .groupBy { it.path }
    .map { (_, dependencies) -> dependencies.first() }
    .toSet()
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
  filter { line ->
    var currentLine = line

    ExternalDockerImages.entries.forEach { dockerImageDescriptor ->
      val variableName = dockerImageDescriptor.variableName()
      val oldValue = $$"${$$variableName}"
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
  doFirst {
    filterAllExternalDockerImages()
  }
}

/**
 * Returns the standard output as a string - throws an exception if the execution failed
 */
fun ExecOutput.standardOutputAsStringOnSuccess(): String {
  return standardOutputAsBytesOnSuccess().decodeToString()
}

/**
 * Returns the standard output as bytes - throws an exception if the execution failed
 */
fun ExecOutput.standardOutputAsBytesOnSuccess(): ByteArray {
  val result = result.get()
  result.rethrowFailure()

  return standardOutput.asBytes.get()
}

/**
 * Returns the error output as a string
 */
fun ExecOutput.errorOutputAsString(): String {
  return errorOutputAsBytes().decodeToString()
}

/**
 * Returns the error output as bytes
 */
fun ExecOutput.errorOutputAsBytes(): ByteArray {
  return standardError.asBytes.get()
}

/**
 * Configure this project to merge the kover reports
 */
fun Project.mergeKoverReports() {
  var addedKoverDependenciesCount = 0
  dependencies {
    subprojects {
      if (plugins.hasPlugin(Plugins.kover)) {
        //Add "kover" dependency to allow merging the kover reports
        //This is the same as `kover(project("my-sub-project"))`
        add("kover", this)
        addedKoverDependenciesCount++
      }
    }
  }

  require(addedKoverDependenciesCount > 0) {
    "No kover dependencies added for project $path. " +
      "Please ensure that the subprojects have the kover plugin applied and that they are configured correctly."
  }
}
