package it.neckar.gradle

import it.neckar.gradle.frontend.applyFrontendProject
import it.neckar.projects.ConfiguredProject
import it.neckar.projects.GradleProjectPath
import it.neckar.projects.ProjectRole
import it.neckar.projects.ProjectRoot
import it.neckar.projects.ProjectType
import org.gradle.api.Project

/**
 * Applies the standard configuration of its type and the plugin of its role to every registered project at
 * or below [baseProject]. Call from the build script of a top directory (`internal/build.gradle.kts`, `tools/build.gradle.kts`),
 * which Gradle evaluates before the build scripts below it, so those see the plugin extensions.
 */
fun ProjectRoot.configureProjects(baseProject: Project) {
  val projects: List<ConfiguredProject> = projectsAtOrBelow(GradleProjectPath(baseProject.path))
  fun ofType(type: ProjectType): List<ConfiguredProject> = projects.filter { it.type == type }

  baseProject.configure(ofType(ProjectType.KotlinMultiplatform)) {
    baseProject.logger.debug("Configuring multiplatform project ${this.path} for targets ${this.targets}")
    ProjectConfiguration.configureMultiplatform(this.getProject(baseProject), this)
  }

  baseProject.configure(ofType(ProjectType.KspProcessor)) {
    baseProject.logger.debug("Configuring KSP processor project: ${this.path}")
    ProjectConfiguration.configureKspProcessor(this.getProject(baseProject))
  }

  baseProject.configure(projects.filter { it.type is ProjectType.Pnpm }) {
    baseProject.logger.debug("Configuring pnpm project: ${this.path}")
    ProjectConfiguration.configurePnpm(this.getProject(baseProject))
  }

  baseProject.configure(ofType(ProjectType.Python)) {
    baseProject.logger.debug("Configuring python project: ${this.path}")
    ProjectConfiguration.configurePython(this.getProject(baseProject))
  }

  baseProject.configure(ofType(ProjectType.KotlinJvm)) {
    baseProject.logger.debug("Configuring jvm project: ${this.path}")
    ProjectConfiguration.configureJvm(this.getProject(baseProject))
  }

  baseProject.configure(ofType(ProjectType.IdeaPlugin)) {
    // IntelliJ IDEA Plugin projects have no shared configuration yet
  }

  baseProject.configure(ofType(ProjectType.Intermediate)) {
    baseProject.logger.debug("Configuring intermediate project: ${this.path}")
    // intermediate projects do not receive any configuration
  }

  baseProject.configure(projects.filter { it.role == ProjectRole.Frontend }) {
    baseProject.logger.debug("Configuring frontend project: ${this.path}")
    this.getProject(baseProject).applyFrontendProject(this.type)
  }

  // Parent projects must be configured *after* their children.
  baseProject.configure(ofType(ProjectType.ProjectParent)) {
    baseProject.logger.debug("Configuring parent project: ${this.path}")
    ProjectConfiguration.configureParentProject(this.getProject(baseProject))
  }
}
