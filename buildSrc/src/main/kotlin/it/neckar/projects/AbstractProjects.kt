package it.neckar.projects

import it.neckar.gradle.GradleContext
import it.neckar.gradle.ansiConsole
import kotlinx.serialization.Serializable
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.assign

/**
 * Abstract base class for objects that contain constants for all projects
 */
abstract class AbstractProjects {

  /**
   * Contains all configured projects
   */
  private val configuredProjects = mutableListOf<ConfiguredProject>()
  private val path2project = mutableMapOf<GradleProjectPath, ConfiguredProject>()

  protected fun configureProject(
    path: GradleProjectPath,
    projectType: ProjectType,
    targets: Set<KotlinTarget> = emptySet(),
  ): ConfiguredProject {
    require(findOrNull(path) == null) { "Project $path already configured" }

    require(targets.isEmpty() || projectType == ProjectType.KotlinMultiplatform) {
      "Project $path of type $projectType must not declare Kotlin targets $targets — only ${ProjectType.KotlinMultiplatform} carries a target set"
    }

    return ConfiguredProject(
      path = path,
      type = projectType,
      targets = targets,
    ).also {
      configuredProjects.add(it)
      path2project[path] = it
    }
  }

  protected fun configureProject(
    path: String,
    projectType: ProjectType,
    targets: Set<KotlinTarget> = emptySet(),
  ): ConfiguredProject {
    return configureProject(GradleProjectPath(path), projectType, targets)
  }

  protected fun jvm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KotlinJvm)
  }

  protected fun kspProcessor(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.KspProcessor)
  }

  /**
   * Registers a Kotlin Multiplatform module for exactly the given [targets].
   *
   * The target list is the module's contract: build-logic declares these Kotlin targets and nothing
   * else, and derives the detekt source list, the toolchain, JUnit and Kover from them. Spell every
   * target out — there is no default set, because an implicit "jvm + js" is what previously made
   * `multiplatformJvmOnly` claim a target list its modules did not have.
   */
  protected fun multiplatform(path: String, vararg targets: KotlinTarget): ConfiguredProject {
    require(targets.isNotEmpty()) { "Multiplatform project $path must declare at least one Kotlin target" }
    require(targets.size == targets.distinct().size) { "Multiplatform project $path declares a duplicate Kotlin target: ${targets.toList()}" }

    return configureProject(path, ProjectType.KotlinMultiplatform, targets.toSet())
  }

  /**
   * A pnpm package that is not a runnable web app (config or library) — no dev server.
   * Use [vite] / [astro] for web apps so local development picks the right dev-server strategy.
   */
  protected fun pnpm(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Pnpm.Library)
  }

  /**
   * A pnpm web app served by a Vite dev server during local development.
   */
  protected fun vite(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Pnpm.Vite)
  }

  /**
   * A pnpm web app served by an Astro dev server during local development.
   */
  protected fun astro(path: String): ConfiguredProject {
    return configureProject(path, ProjectType.Pnpm.Astro)
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
   * Returns all multiplatform projects, whatever targets they are registered for.
   */
  fun multiplatformProjects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinMultiplatform)
  }

  fun project(type: ProjectType): List<ConfiguredProject> {
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
    return configuredProjects.filter { it.type is ProjectType.Pnpm }
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
  return findConfiguredProject().type is ProjectType.Pnpm
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

/**
 * Finds the [ConfiguredProject] for this Gradle [Project] across all project registries
 * ([Projects], [ExternalProjects], [OtherProjects]), or null when the project is not registered.
 * Only the root project is legitimately unregistered — every *sub*project is registered in exactly
 * one registry (enforced by [verifyProjectsConfigured]).
 */
fun Project.findConfiguredProjectOrNull(): ConfiguredProject? {
  return Projects.findOrNull(this)
    ?: ExternalProjects.findOrNull(this)
    ?: OtherProjects.findOrNull(this)
}

/**
 * Finds the [ConfiguredProject] for this Gradle [Project] across all project registries
 * ([Projects], [ExternalProjects], [OtherProjects]). Every project in the build is registered in
 * exactly one of them — enforced by [verifyProjectsConfigured] — so a missing entry is a
 * configuration error and throws rather than returning null.
 */
fun Project.findConfiguredProject(): ConfiguredProject {
  return findConfiguredProjectOrNull()
    ?: throw IllegalStateException("Project $path is not registered in Projects, ExternalProjects or OtherProjects")
}

fun Project.isOfType(projectType: ProjectType): Boolean {
  return findConfiguredProject().type == projectType
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
   * The Kotlin targets this module is built for. Empty for every type except
   * [ProjectType.KotlinMultiplatform].
   */
  val targets: Set<KotlinTarget>,
) {

  /**
   * Whether [KotlinTarget.Jvm] is in the registered target set — decides toolchain, JUnit and Kover
   * for a multiplatform module. False for every other type, [ProjectType.KotlinJvm] included: those
   * carry no target set and are configured by their own path.
   */
  val hasJvmTarget: Boolean
    get() = targets.contains(KotlinTarget.Jvm)

  /**
   * Returns the project using the given [resolver]
   */
  fun getProject(resolver: Project): Project {
    try {
      return resolver.project(path.path)
    } catch (e: org.gradle.api.UnknownProjectException) {
      resolver.logger.warn("Could not find project ${resolver.ansiConsole.red(path.path)}")
      resolver.logger.warn("Check settings.gradle.kts if the project has been added")
      resolver.logger.warn("Try ${resolver.ansiConsole.orange("gradle clean build --no-configuration-cache --no-daemon --no-build-cache")} to force recompilation")
      throw e
    } catch (e: Exception) {
      resolver.logger.error("Unexpected exception of type ${e.javaClass.simpleName} while resolving project $path")
      throw e
    }
  }

  /**
   * Returns the Gradle [Project] (uses [GradleContext] global state).
   */
  fun project(): Project {
    return GradleContext.project(path.path)
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


/**
 * The nature of a configured project. Sealed so the dev-server strategy for JS projects can be
 * expressed as a subtype of [Pnpm] rather than a parallel flag (see [it.neckar.gradle.localdev]).
 */
sealed interface ProjectType {
  /**
   * JVM project - with the latest LTS JDK version
   */
  data object KotlinJvm : ProjectType

  /**
   * Projects that contain a KSP processor
   */
  data object KspProcessor : ProjectType

  /**
   * Multiplatform project. Which platforms it actually builds for is the registered
   * [ConfiguredProject.targets] set, not a property of the type — a module compiling only to
   * `linuxX64` and one compiling to `jvm` + `js` + `wasmJs` are both of this type.
   */
  data object KotlinMultiplatform : ProjectType

  /**
   * Python project
   */
  data object Python : ProjectType

  /**
   * IntelliJ IDEA plugin
   */
  data object IdeaPlugin : ProjectType

  /**
   * Represents a project parent (not the Gradle root).
   * A project parent is used to merge things related to one project (e.g. deployment or kover reports)
   */
  data object ProjectParent : ProjectType

  /**
   * Intermediate project - does not have any configuration
   */
  data object Intermediate : ProjectType

  /**
   * Another project - does not have any (common) configuration
   */
  data object Other : ProjectType

  /**
   * JS project built with pnpm. The subtype is the local-dev server strategy; [Library] has none.
   */
  sealed interface Pnpm : ProjectType {
    /** Generic pnpm package (config or library) — not a runnable web app, no dev server. */
    data object Library : Pnpm

    /** Web app with a Vite dev server (`VITE_PORT=<port> pnpm exec vite`). */
    data object Vite : Pnpm

    /** Web app with an Astro dev server (`pnpm exec astro dev --port <port> --host`). */
    data object Astro : Pnpm
  }

  companion object {
    /**
     * All concrete project types — replaces the former enum `entries`.
     */
    val all: List<ProjectType> = listOf(
      KotlinJvm,
      KspProcessor,
      KotlinMultiplatform,
      Python,
      IdeaPlugin,
      ProjectParent,
      Intermediate,
      Other,
      Pnpm.Library,
      Pnpm.Vite,
      Pnpm.Astro,
    )
  }
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
 * Provides type safety for project paths like `:internal:open:commons:typescript:typescript-utils`.
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
   * Example: `:internal:closed:company` → `internal/closed/company`.
   */
  val filePath: String
    get() = path.removePrefix(":").replace(':', '/')

  /**
   * The path of [taskName] in this project: `:internal:patterns:ktor-backend` + `build` →
   * `:internal:patterns:ktor-backend:build`. The root project (`:`) yields `:build`.
   */
  fun task(taskName: String): GradleTaskPath {
    require(taskName.isNotBlank()) { "Task name must not be blank" }
    return GradleTaskPath(if (path == ":") ":$taskName" else "$path:$taskName")
  }
}
