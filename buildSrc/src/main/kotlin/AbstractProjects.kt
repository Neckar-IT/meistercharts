import it.neckar.gradle.ansiConsole
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.invocation.Gradle
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.assign

/**
 * Abstract base class for objects that contain constants for all projects
 */
abstract class AbstractProjects {

  val disabledProjectsSupport: DisabledProjectsSupport = DisabledProjectsSupport.load(
    disabledProjectsFile = GradleContext.rootProject.file("disabled-projects.json5"),
  )

  /**
   * Contains all configured projects
   */
  private val configuredProjects = mutableListOf<ConfiguredProject>()
  private val path2project = mutableMapOf<GradleProjectPath, ConfiguredProject>()

  protected fun configureProject(path: GradleProjectPath, projectType: ProjectType): ConfiguredProject {
    require(findOrNull(path) == null) { "Project $path already configured" }

    val disabled = disabledProjectsSupport.isDisabled(path.path)
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

  protected fun configureProject(path: String, projectType: ProjectType): ConfiguredProject {
    return configureProject(GradleProjectPath(path), projectType)
  }

  protected fun jvm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm)
  }

  protected fun kspProcessor(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KspProcessor)
  }

  protected fun multiPlatformLts(path: String): ConfiguredProject {
    return multiPlatform(path, JvmType.JavaLatestLTS)
  }

  protected fun multiPlatform(path: String, jvmType: JvmType = JvmType.JavaLatestLTS): ConfiguredProject {
    val type = when (jvmType) {
      JvmType.JavaLatestLTS -> ProjectType.KotlinMultiplatform
    }
    return configureProject(path, type)
  }

  protected fun multiplatformJvmOnly(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinMultiplatformJvmOnly)
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

  protected fun parent(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.ProjectParent)
  }

  protected fun other(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Other)
  }

  /**
   * Returns the configured project for the given [project]
   */
  fun find(project: Project): ConfiguredProject {
    return find(GradleProjectPath(project.path))
  }

  fun find(path: GradleProjectPath): ConfiguredProject {
    return findOrNull(path) ?: throw IllegalStateException("Project $path not found")
  }

  fun find(path: String): ConfiguredProject {
    return find(GradleProjectPath(path))
  }

  fun findOrNull(project: Project): ConfiguredProject? {
    return findOrNull(GradleProjectPath(project.path))
  }

  /**
   * Returns the configured project for the given [path].
   * Returns null if the project is not found.
   */
  fun findOrNull(path: GradleProjectPath): ConfiguredProject? {
    return path2project[path]
  }

  fun findOrNull(path: String): ConfiguredProject? {
    return findOrNull(GradleProjectPath(path))
  }

  /**
   * Returns all multi-platform projects for the current java version
   */
  fun multiPlatformProjectsLTS(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.KotlinMultiplatform && it.enabled }
  }

  /**
   * Returns all JVM-only multi-platform projects
   */
  fun multiPlatformJvmOnlyProjectsLTS(): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == ProjectType.KotlinMultiplatformJvmOnly && it.enabled }
  }

  fun project(type: ProjectType): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == type && it.enabled }
  }

  /**
   * Returns all projects of the given type, including disabled ones.
   * Use [project] for only enabled projects.
   */
  fun projectIncludingDisabled(type: ProjectType): List<ConfiguredProject> {
    return configuredProjects.filter { it.type == type }
  }

  fun jvmProjects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinJvm)
  }

  fun parents(): List<ConfiguredProject> {
    return project(ProjectType.ProjectParent)
  }

  fun kspProcessorProjects(): List<ConfiguredProject> {
    return project(ProjectType.KspProcessor)
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

  /**
   * Configures the projects based on the provided project.
   * Call this method in your "root" build script to configure all projects.
   *
   * Note: All project list methods (e.g., jvmProjects(), multiPlatformProjectsLTS()) now
   * return only enabled projects, so disabled projects are automatically skipped.
   */
  fun configureProjects(baseProject: Project) {
    /**
     * Configuration for the multi-platform projects
     */
    baseProject.configure(multiPlatformProjectsLTS()) {
      baseProject.logger.debug("Configuring multi-platform LTS project: ${this.path}")
      ProjectConfiguration.configureMultiPlatform(this.getProject(baseProject), JvmType.JavaLatestLTS)
    }

    baseProject.configure(multiPlatformJvmOnlyProjectsLTS()) {
      baseProject.logger.debug("Configuring multi-platform JVM-only LTS project: ${this.path}")
      ProjectConfiguration.configureMultiPlatformJvmOnly(this.getProject(baseProject), JvmType.JavaLatestLTS)
    }

    baseProject.configure(kspProcessorProjects()) {
      baseProject.logger.debug("Configuring KSP processor project: ${this.path}")
      ProjectConfiguration.configureKspProcessor(this.getProject(baseProject))
    }

    baseProject.configure(pnpmProjects()) {
      baseProject.logger.debug("Configuring pnpm project: ${this.path}")
      ProjectConfiguration.configurePnpm(this.getProject(baseProject))
    }

    baseProject.configure(pythonProjects()) {
      baseProject.logger.debug("Configuring python project: ${this.path}")
      ProjectConfiguration.configurePython(this.getProject(baseProject))
    }

    /**
     * Configuration for the Java projects
     */
    baseProject.configure(jvmProjects()) {
      baseProject.logger.debug("Configuring jvm project: ${this.path}")
      ProjectConfiguration.configureJvm(this.getProject(baseProject))
    }

    /**
     * Configuration for all IntelliJ IDEA Plugin projects
     */
    baseProject.configure(ideaPluginProjects()) {
      //  configureKotlin()
    }

    baseProject.configure(intermediateProjects()) {
      baseProject.logger.debug("Configuring intermediate project: ${this.path}")
      //No configuration
    }

    /**
     * ATTENTION: Call this *after* the other projects have been configured
     */
    baseProject.configure(parents()) {
      baseProject.logger.debug("Configuring parent project: ${this.path}")
      ProjectConfiguration.configureParentProject(this.getProject(baseProject))
    }
  }
}

fun Project.isJvmProject(): Boolean {
  return isOfType(ProjectType.KotlinJvm)
}

fun Project.isParentProject(): Boolean {
  return isOfType(ProjectType.ProjectParent)
}

fun Project.isKspProcessorProject(): Boolean {
  return isOfType(ProjectType.KspProcessor)
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
  return this.project(configuredProject.path.path)
}

fun org.gradle.api.artifacts.dsl.DependencyHandler.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path.path)
}

fun Dependencies.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path.path)
}

fun Project.isSandboxProject(): Boolean {
  return this.path.contains(":sandbox:")
}

/**
 * Represents a configured Gradle project
 */
@ConsistentCopyVisibility
data class ConfiguredProject internal constructor(
  /**
   * The path of the project
   */
  val path: GradleProjectPath,
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
      return resolver.project(path.path)
    } catch (e: org.gradle.api.UnknownProjectException) {
      resolver.logger.warn("Could not find project ${resolver.ansiConsole.red(path.path)} - is it disabled?: $disabled")
      resolver.logger.warn("* Check disabled-projects.json if the project has been disabled!")
      resolver.logger.warn("* Check settings.gradle.kts if the project has been added")
      resolver.logger.warn("Try ${resolver.ansiConsole.orange("gradle clean build --no-configuration-cache --no-daemon --no-build-cache")} to force recompilation")
      throw e
    } catch (e: Exception) {
      resolver.logger.error("Unexpected exception of type ${e.javaClass.simpleName} while resolving project $path - is it disabled?: $disabled")
      throw e
    }
  }

  /**
   * Returns the Gradle [Project] (uses [GradleContext] global state).
   */
  fun project(): Project {
    return GradleContext.project(path.path)
  }

  /**
   * Returns the Gradle [Project] using context parameter (avoids global state).
   */
  context(gradle: Gradle)
  fun project(): Project {
    return gradle.rootProject.project(path.path)
  }

  override fun toString(): String {
    return path.path
  }

  /**
   * Creates a task name for this project
   */
  fun task(taskName: String): String {
    return "$path:$taskName"
  }

  /**
   * Returns the "build" task name for this project
   */
  val buildTask: String
    get() {
      return task("build")
    }
}


enum class ProjectType {
  /**
   * JVM project - with the latest LTS JDK version
   */
  KotlinJvm,

  /**
   * Projects that contain a KSP processor
   */
  KspProcessor,

  /**
   * Multiplatform project - usually contains JVM and JS code
   */
  KotlinMultiplatform,

  /**
   * Multiplatform project with JVM-only target.
   * Code remains in commonMain for strategic flexibility, but no JS compilation.
   */
  KotlinMultiplatformJvmOnly,

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
   * Represents a project parent (not the Gradle root).
   * A project parent is used to merge things related to one project (e.g. deployment or kover reports)
   */
  ProjectParent,

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
  return containsByPath(GradleProjectPath(project.path))
}

fun List<ConfiguredProject>.containsByPath(expectedProjectPath: GradleProjectPath): Boolean {
  return this.any { it.path == expectedProjectPath }
}

fun List<ConfiguredProject>.containsByPath(expectedProjectPath: String): Boolean {
  return containsByPath(GradleProjectPath(expectedProjectPath))
}


/**
 * Value class representing a Gradle project path.
 *
 * Provides type safety for project paths like `:internal:open:commons:typescript-utils`.
 */
@Serializable
@JvmInline
value class GradleProjectPath(val path: String) {
  init {
    require(path.startsWith(":")) { "Gradle project path must start with ':': $path" }
    require(path.isNotBlank()) { "Gradle project path must not be blank" }
  }

  override fun toString(): String = path

  /**
   * The project's location as a filesystem-relative path (relative to the Gradle root).
   *
   * Example: `:internal:closed:profiles` → `internal/closed/profiles`.
   */
  val filePath: String
    get() = path.removePrefix(":").replace(':', '/')
}
