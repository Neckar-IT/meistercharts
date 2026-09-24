package it.neckar.projects

import org.gradle.api.Project

/**
 * The root of a project registry: the Gradle root project `:`, with lookups over every registered
 * project below it.
 */
abstract class ProjectRoot : ConfiguredProject(parent = null, relativePath = GradleProjectPath.Root, type = ProjectType.Intermediate, role = ProjectRole.Container) {

  /** Every registered project, parents before their subprojects; the root itself is no registration. */
  val allProjects: List<ConfiguredProject> by lazy {
    selfAndDescendants.drop(1)
  }

  private val projectsByPath: Map<GradleProjectPath, ConfiguredProject> by lazy {
    allProjects.groupBy { it.path }.mapValues { (path, projects) ->
      require(projects.size == 1) { "Project $path already configured" }
      projects.single()
    }
  }

  fun findOrNull(path: GradleProjectPath): ConfiguredProject? {
    return projectsByPath[path]
  }

  fun findOrNull(path: String): ConfiguredProject? {
    return findOrNull(GradleProjectPath(path))
  }

  fun findOrNull(project: Project): ConfiguredProject? {
    return findOrNull(GradleProjectPath(project.path))
  }

  fun find(path: GradleProjectPath): ConfiguredProject {
    return findOrNull(path) ?: throw IllegalStateException("Project $path not found")
  }

  fun find(path: String): ConfiguredProject {
    return find(GradleProjectPath(path))
  }

  fun find(project: Project): ConfiguredProject {
    return find(GradleProjectPath(project.path))
  }

  /**
   * The registered projects at or below [path]: `:internal` yields every project a build script of
   * `internal/` configures.
   */
  fun projectsAtOrBelow(path: GradleProjectPath): List<ConfiguredProject> {
    return allProjects.filter { path.relativizeOrNull(it.path) != null }
  }

  fun project(type: ProjectType): List<ConfiguredProject> {
    return allProjects.filter { it.type == type }
  }

  /**
   * Returns all multiplatform projects, whatever targets they are registered for.
   */
  fun multiplatformProjects(): List<ConfiguredProject> {
    return project(ProjectType.KotlinMultiplatform)
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
    return allProjects.filter { it.type is ProjectType.Pnpm }
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
