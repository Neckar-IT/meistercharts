import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.kotlin.dsl.project

/**
 * Abstract base class for objects that contain constants for all projects
 */
abstract class AbstractProjects {

  val disabledProjectsSupport: DisabledProjectsSupport = DisabledProjectsSupport.load(
    disabledProjectsFile = GradleContext.rootProject.file("disabled-projects.json5"),
    disabledOnMacOsFile = GradleContext.rootProject.file(".project-sets/${ProjectType.KotlinJvm8Fx.name}.json5"),
  )

  /**
   * Contains all configured projects
   */
  private val configuredProjects = mutableListOf<ConfiguredProject>()
  private val path2project = mutableMapOf<String, ConfiguredProject>()

  protected fun configureProject(path: String, projectType: ProjectType): ConfiguredProject {
    require(path2project[path] == null) { "Project $path already configured" }

    val disabled = disabledProjectsSupport.isDisabled(path)
    if (disabled && projectType == ProjectType.PNPM) {
      throw IllegalStateException("Disabling PNPM projects is not supported!. Project $path is disabled!\nCheck disabled-projects.json5! Add to forceEnabled if necessary!")
    }

    return ConfiguredProject(
      path = path,
      type = projectType,
      disabled = disabled
    ).also {
      configuredProjects.add(it)
      path2project[path] = it
    }
  }

  protected fun jvm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm)
  }

  protected fun jvm8(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm8)
  }

  protected fun jvm8Fx(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm8Fx)
  }

  //TODO introduce JVM version parameter
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

  fun project(type: ProjectType): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == type }
  }

  fun jvmProjects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinJvm)
  }

  fun jvm8Projects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinJvm8)
  }

  fun jvm8FxProjects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinJvm8Fx)
  }

  fun pnpmProjects(): List<ConfiguredProject> {
    return project(ProjectType.PNPM)
  }

  fun pythonProjects(): List<ConfiguredProject> {
    return project(ProjectType.Python)
  }

  fun ideaPluginProjects(): List<ConfiguredProject> {
    return project(ProjectType.IdeaPlugin)
  }

  fun intermediateProjects(): List<ConfiguredProject> {
    return project(ProjectType.Intermediate)
  }

  fun otherProjects(): List<ConfiguredProject> {
    return project(ProjectType.Other)
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

  /**
   * Set to true if this project has been disabled
   */
  val disabled: Boolean,
) {

  val enabled: Boolean
    get() = disabled.not()

  /**
   * Returns the project using the given [resolver]
   */
  fun getProject(resolver: Project): Project {
    try {
      return resolver.project(path)
    } catch (e: org.gradle.api.UnknownProjectException) {
      println("Could not find project $path - is it disabled?: $disabled")
      println("Check disabled-projects.json if the project has been disabled!")
      println("Try `gradle clean build --no-configuration-cache --no-daemon --no-build-cache` to force recompilation")
      throw e
    } catch (e: Exception) {
      println("Unexpected exception of type ${e.javaClass.simpleName} while resolving project $path - is it disabled?: $disabled")
      throw e
    }
  }

  //TODO replace with context once supported
  fun project(): Project {
    return GradleContext.project(path)
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
   * JVM project - with the current JDK version
   */
  KotlinJvm,

  /**
   * Represents an old JVM 8 project (without JavaFX)
   */
  KotlinJvm8,

  /**
   * JVM Project that uses JavaFX (Oracle JDK 8)
   */
  KotlinJvm8Fx,

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


/**
 * Returns true if the list contains a project with the same path as the given project.
 */
fun List<ConfiguredProject>.containsByPath(project: Project): Boolean {
  val expectedPath = project.path
  return containsByPath(expectedPath)
}

fun List<ConfiguredProject>.containsByPath(expectedProjectPath: String): Boolean {
  return this.any { it.path == expectedProjectPath }
}
