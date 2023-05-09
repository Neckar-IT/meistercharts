import com.google.common.io.Files
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ComponentArtifactIdentifier
import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.getByName
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
fun Project.copySourcesJarToDir(
  targetDir: File,
  sourcesJarTaskName: String = "jvmSourcesJar"
) {
  findSourcesJar(sourcesJarTaskName)?.let { sourcesJar ->
    sourcesJar.ensureExists("sourcesJar")
    val targetFile = File(targetDir, path.projectPath2FileNameWithCedarsoft("-sources"))
    Files.copy(sourcesJar, targetFile)
  }
}


fun File.ensureExists(taskNameToCreate: String) {
  if (!exists()) {
    throw GradleException("File <${this.absolutePath}> does not exist. Run `gradle $taskNameToCreate` for all projects before")
  }
}

/**
 * Converts a component identifier to a file name.
 * This method can be used when copying dependencies
 */
fun ComponentIdentifier.toFileName(suffix: String = ""): String {
  return when (val identifier = this) {
    is ProjectComponentIdentifier -> identifier.projectPath.projectPath2FileNameWithCedarsoft(suffix)
    is ModuleComponentIdentifier -> "${identifier.group}.${identifier.module}$suffix.jar"
    else -> throw IllegalArgumentException("identifier invalid $identifier::class")
  }
}

/**
 * Copies the resources files for a project.
 * Workaround for:
 * https://youtrack.jetbrains.com/issue/KT-38230/Gradle-MPP-JS-Process-JS-resources-from-dependencies
 */
fun Copy.copyJSResources(configurationNames: List<String> = listOf("runtimeClasspath", "commonMainApi", "jsRuntimeClasspath")) {
  val projectDependencies = this.project.findAllProjectDependencies(configurationNames)

  fun Copy.addCopyRef(dependency: Project) {
    dependency.pluginManager.withPlugin(Plugins.kotlinMultiPlatform) {
      dependency.tasks.getByName("jsProcessResources").let { task ->
        val sourceDir = (task as Copy).destinationDir
        inputs.files(sourceDir)
        dependsOn(task)
        from(sourceDir)
      }
    }
    dependency.pluginManager.withPlugin(Plugins.kotlinJs) {
      dependency.tasks.getByName("processResources").let { task ->
        val sourceDir = (task as Copy).destinationDir
        inputs.files(sourceDir)
        dependsOn(task)
        from(sourceDir)
      }
    }
  }

  projectDependencies.forEach { dependency ->
    when {
      dependency.state.executed -> {
        addCopyRef(dependency)
      }

      else -> {
        dependency.afterEvaluate {
          addCopyRef(dependency)
        }
      }
    }
  }
}

/**
 * Returns true if this project is a kotlin multiplatform project.
 */
fun Project.hasKotlinMultiplatformPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinMultiPlatform)
}

fun Project.hasKotlinJsPlugin(): Boolean {
  return hasPlugin(Plugins.kotlinJs)
}

fun Project.hasPlugin(kotlinMultiPlatform: String): Boolean {
  return this.pluginManager.findPlugin(kotlinMultiPlatform) != null
}

/**
 * Copy the resources from JVM
 */
fun Copy.copyJvmResources(configurationNames: List<String> = listOf("runtimeClasspath", "commonMainApi", "jvmRuntimeClasspath")) {
  val projectDependencies = this.project.findAllProjectDependencies(configurationNames)

  fun Copy.addCopyRef(dependency: Project) {
    dependency.pluginManager.withPlugin(Plugins.kotlinMultiPlatform) {
      dependency.tasks.getByName("jvmProcessResources").let { task ->
        val sourceDir = (task as Copy).destinationDir
        inputs.files(sourceDir)
        dependsOn(task)
        from(sourceDir)
      }
    }
    dependency.pluginManager.withPlugin(Plugins.kotlinJs) {
      dependency.tasks.getByName("processResources").let { task ->
        val sourceDir = (task as Copy).destinationDir
        inputs.files(sourceDir)
        dependsOn(task)
        from(sourceDir)
      }
    }
  }

  projectDependencies.forEach { dependency ->
    when {
      dependency.state.executed -> {
        addCopyRef(dependency)
      }

      else -> {
        dependency.afterEvaluate {
          addCopyRef(dependency)
        }
      }
    }
  }
}

/**
 * Returns all project dependencies (including transitive dependencies), that:
 * * have a configuration
 */
private fun Project.findAllProjectDependencies(
  configurationNames: List<String>,
  foundProjects: MutableSet<Project> = mutableSetOf(),
  visitedProjects: MutableSet<Project> = mutableSetOf(),
): Set<Project> {
  if (this in visitedProjects) {
    return foundProjects
  }

  visitedProjects.add(this)

  configurationNames.forEach { configurationName ->
    val configuration = configurations.findByName(configurationName)

    if (configuration != null) {
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
private fun Configuration.findDirectProjectDependencies(): List<Project> {
  return allDependencies
    .filterIsInstance<ProjectDependency>()
    .map { it.dependencyProject }
}

/**
 * Updates the process resources task to also copy the resources from the dependencies
 */
fun Project.alsoCopyJsResourcesOfDependentProjects() {
  val processResourcesTask: Copy = tasks.getByName<Copy>("processResources")

  val copyResourcesFromDeps = tasks.create("copyResourcesFromDeps", Copy::class.java) {
    copyJSResources()
    destinationDir = processResourcesTask.destinationDir
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE //necessary since we depend on other JS projects which already have copied all resources
  }

  processResourcesTask.dependsOn(copyResourcesFromDeps)
}

/**
 * Copy resources for JVM projects
 */
fun Project.alsoCopyJvmResourcesOfDependentProjects() {
  val processResourcesTask: Copy = tasks.getByName<Copy>("processResources")

  val copyResourcesFromDeps = tasks.create("copyResourcesFromDeps", Copy::class.java) {
    copyJvmResources()
    destinationDir = processResourcesTask.destinationDir
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE //necessary since we depend on other JS projects which already have copied all resources
  }

  processResourcesTask.dependsOn(copyResourcesFromDeps)
}
