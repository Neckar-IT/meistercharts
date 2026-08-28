package it.neckar.gradle

import it.neckar.projects.AbstractProjects
import org.gradle.api.Project

/**
 * Applies the standard configuration to every project category exposed by [AbstractProjects].
 * Call from the "root" build script (`internal/build.gradle.kts`, `external/build.gradle.kts`).
 */
fun AbstractProjects.configureProjects(baseProject: Project) {
  baseProject.configure(multiplatformProjects()) {
    baseProject.logger.debug("Configuring multiplatform project ${this.path} for targets ${this.targets}")
    ProjectConfiguration.configureMultiplatform(this.getProject(baseProject), this)
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

  baseProject.configure(jvmProjects()) {
    baseProject.logger.debug("Configuring jvm project: ${this.path}")
    ProjectConfiguration.configureJvm(this.getProject(baseProject))
  }

  baseProject.configure(ideaPluginProjects()) {
    // IntelliJ IDEA Plugin projects have no shared configuration yet
  }

  baseProject.configure(intermediateProjects()) {
    baseProject.logger.debug("Configuring intermediate project: ${this.path}")
    // intermediate projects do not receive any configuration
  }

  // Parent projects must be configured *after* their children.
  baseProject.configure(parents()) {
    baseProject.logger.debug("Configuring parent project: ${this.path}")
    ProjectConfiguration.configureParentProject(this.getProject(baseProject))
  }
}
