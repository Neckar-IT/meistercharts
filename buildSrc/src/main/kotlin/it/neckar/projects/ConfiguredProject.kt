package it.neckar.projects

import org.gradle.api.Project

/**
 * A registered Gradle project. A container project declares its subprojects as properties named
 * after their directories, so `Projects.open.commons.kotlinLang` mirrors `internal/open/commons/kotlin-lang`.
 */
open class ConfiguredProject protected constructor(
  parent: ConfiguredProject?,
  /**
   * The path below [parent]: one directory for a subproject, several for a group that skips an
   * unnamed level (`:internal:open` below the root)
   */
  val relativePath: GradleProjectPath,
  val type: ProjectType,
  /**
   * The Kotlin targets this module is built for. Empty for every type except
   * [ProjectType.KotlinMultiplatform].
   */
  val targets: Set<KotlinTarget> = emptySet(),
) {

  /**
   * The full Gradle path: [OpenProjects] lies below `:internal:open` in the monorepo and below `:` in
   * a build rooted at `internal/open`.
   */
  val path: GradleProjectPath = parent?.path?.resolve(relativePath) ?: relativePath

  private val registeredSubprojects: MutableList<ConfiguredProject> = mutableListOf()

  /** The subprojects in registration order. */
  val subprojects: List<ConfiguredProject>
    get() = registeredSubprojects

  init {
    require(targets.isEmpty() || type == ProjectType.KotlinMultiplatform) {
      "Project $path of type $type must not declare Kotlin targets $targets — only ${ProjectType.KotlinMultiplatform} carries a target set"
    }
    parent?.registeredSubprojects?.add(this)
  }

  /** This project and every project below it, parents before their subprojects. */
  val selfAndDescendants: List<ConfiguredProject>
    get() = listOf(this) + registeredSubprojects.flatMap { it.selfAndDescendants }

  /**
   * Whether [KotlinTarget.Jvm] is in the registered target set — decides toolchain, JUnit and Kover
   * for a multiplatform module. False for every other type, [ProjectType.KotlinJvm] included: those
   * carry no target set and are configured by their own path.
   */
  val hasJvmTarget: Boolean
    get() = targets.contains(KotlinTarget.Jvm)

  protected fun jvm(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.KotlinJvm)
  }

  protected fun kspProcessor(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.KspProcessor)
  }

  /**
   * Registers a Kotlin Multiplatform module for exactly the given [targets].
   *
   * The target list is the module's contract: build-logic declares these Kotlin targets and nothing
   * else, and derives the detekt source list, the toolchain, JUnit and Kover from them. Spell every
   * target out — there is no default set, because an implicit "jvm + js" is what previously made
   * `multiplatformJvmOnly` claim a target list its modules did not have.
   */
  protected fun multiplatform(directory: String, vararg targets: KotlinTarget): ConfiguredProject {
    require(targets.isNotEmpty()) { "Multiplatform project $directory must declare at least one Kotlin target" }
    require(targets.size == targets.distinct().size) { "Multiplatform project $directory declares a duplicate Kotlin target: ${targets.toList()}" }

    return subproject(directory, ProjectType.KotlinMultiplatform, targets.toSet())
  }

  /**
   * A pnpm package without a Vite or Astro dev server: config, library, or a site another generator
   * builds (Eleventy). Use [vite] / [astro] for web apps so local development picks the right
   * dev-server strategy.
   */
  protected fun pnpm(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Pnpm.Library)
  }

  /**
   * A pnpm web app served by a Vite dev server during local development.
   */
  protected fun vite(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Pnpm.Vite)
  }

  /**
   * A pnpm web app served by an Astro dev server during local development.
   */
  protected fun astro(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Pnpm.Astro)
  }

  protected fun python(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Python)
  }

  protected fun ideaPlugin(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.IdeaPlugin)
  }

  protected fun intermediate(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Intermediate)
  }

  protected fun parent(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.ProjectParent)
  }

  protected fun other(directory: String): ConfiguredProject {
    return subproject(directory, ProjectType.Other)
  }

  private fun subproject(directory: String, type: ProjectType, targets: Set<KotlinTarget> = emptySet()): ConfiguredProject {
    return ConfiguredProject(this, GradleProjectPath(":$directory"), type, targets)
  }

  /**
   * Returns the project using the given [resolver]
   */
  fun getProject(resolver: Project): Project {
    try {
      return resolver.project(path.path)
    } catch (e: org.gradle.api.UnknownProjectException) {
      resolver.logger.warn("Could not find project ${path.path}")
      resolver.logger.warn("Check settings.gradle.kts if the project has been added")
      resolver.logger.warn("Try gradle clean build --no-configuration-cache --no-daemon --no-build-cache to force recompilation")
      throw e
    } catch (e: Exception) {
      resolver.logger.error("Unexpected exception of type ${e.javaClass.simpleName} while resolving project $path")
      throw e
    }
  }

  override fun toString(): String {
    return path.path
  }

  /**
   * Creates a task name for this project
   */
  fun task(taskName: String): String {
    return path.task(taskName).path
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
