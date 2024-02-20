import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.support.delegates.ProjectDelegate

/**
 * Abstract base class for objects that contain constants for all projects
 */
abstract class AbstractProjects {
  /**
   * Contains all configured projects
   */
  private val configuredProjects = mutableListOf<ConfiguredProject>()
  private val path2project = mutableMapOf<String, ConfiguredProject>()

  protected fun configureProject(path: String, projectType: ProjectType): ConfiguredProject {
    require(path2project.get(path) == null) { "Project $path already configured" }

    return ConfiguredProject(path, projectType).also {
      configuredProjects.add(it)
      path2project[path] = it
    }
  }

  protected fun jvm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm)
  }

  protected fun multiPlatform(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinMultiplatform)
  }

  protected fun pnpm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.PNPM)
  }

  protected fun python(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Python)
  }

  protected fun ideaPlugin(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.IdeaPlugin)
  }

  protected fun intermediate(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Intermediate)
  }

  protected fun other(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Other)
  }

  /**
   * Returns the configured project for the given [project]
   */
  fun find(project: Project): ConfiguredProject {
    val path = project.path
    return path2project[path] ?: throw IllegalStateException("Project $path not found")
  }

  fun findOrNull(project: Project): ConfiguredProject? {
    val path = project.path
    return path2project[path]
  }

  /**
   * Returns all multi platform projects
   */
  fun multiPlatformProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.KotlinMultiplatform }
  }

  fun jvmProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.KotlinJvm }
  }

  fun pnpmProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.PNPM }
  }

  fun pythonProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.Python }
  }

  fun ideaPluginProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.IdeaPlugin }
  }

  fun intermediateProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.Intermediate }
  }

  fun otherProjects(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.Other }
  }

}


fun Project.isMultiplatformProject(): Boolean {
  return isOfType(ProjectType.KotlinMultiplatform)
}

fun Project.isJvmProject(): Boolean {
  return isOfType(ProjectType.KotlinJvm)
}

/**
 * Returns true if this is a pnpm project
 */
fun Project.isPnpmProject(): Boolean {
  return isOfType(ProjectType.PNPM)
}

fun Project.isIdeaPluginProject(): Boolean {
  return isOfType(ProjectType.IdeaPlugin)
}

fun Project.isPythonProject(): Boolean {
  return isOfType(ProjectType.Python)
}

fun Project.isIntermediateProject(): Boolean {
  return isOfType(ProjectType.Intermediate)
}

fun Project.isOtherProject(): Boolean {
  return isOfType(ProjectType.Other)
}

fun Project.isOfType(projectType: ProjectType): Boolean {
  val internalProject = Projects.findOrNull(this)
  val externalProject = ExternalProjects.findOrNull(this)
  val otherProject = OtherProjects.findOrNull(this)

  if (internalProject != null) {
    return internalProject.type == projectType
  }

  if (externalProject != null) {
    return externalProject.type == projectType
  }

  if (otherProject != null) {
    return otherProject.type == projectType
  }

  throw IllegalStateException("Project $path not found")
}

fun Project.project(configuredProject: ConfiguredProject): Project {
  return this.project(configuredProject.path)
}

fun org.gradle.api.artifacts.dsl.DependencyHandler.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path)
}

fun Dependencies.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path)
}

fun Project.isSandboxProject(): Boolean {
  return this.path.contains(":sandbox:")
}

/**
 * Represents a configured Gradle project
 */
data class ConfiguredProject internal constructor(
  /**
   * The path of the project
   */
  val path: String,
  /**
   * The type of the project
   */
  val type: ProjectType,
) {
  //context(ProjectDelegate)
  fun getProject(resolver: ProjectDelegate): Project {
    return resolver.project(path)
  }

  fun getProject(resolver: Project): Project {
    return resolver.project(path)
  }

  override fun toString(): String {
    return path
  }

  /**
   * Creates a task name for this project
   */
  fun task(taskName: String): String {
    return "$path:$taskName"
  }
}


enum class ProjectType {
  /**
   * JVM project
   */
  KotlinJvm,

  /**
   * Multiplatform project - usually contains JVM and JS code
   */
  KotlinMultiplatform,

  /**
   * JS project - build using pnpm
   */
  PNPM,

  /**
   * Python project
   */
  Python,

  /**
   * IntelliJ IDEA plugin
   */
  IdeaPlugin,

  /**
   * Intermediate project - does not have any configuration
   */
  Intermediate,

  /**
   * Another project - does not have any (common) configuration
   */
  Other,
}
