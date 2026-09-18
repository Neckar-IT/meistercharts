package it.neckar.projects

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.dsl.Dependencies
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

fun Project.project(configuredProject: ConfiguredProject): Project {
  return this.project(configuredProject.path.path)
}

fun DependencyHandler.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path.path)
}

fun Dependencies.project(configuredProject: ConfiguredProject): ProjectDependency {
  return this.project(configuredProject.path.path)
}
