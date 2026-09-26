package it.neckar.gradle.pnpm.dependency

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.PathSensitivity

/**
 * The Gradle projects providing the `workspace:` packages a pnpm module imports, without the module
 * itself.
 */
@JvmInline
value class WorkspaceDependencyProjects internal constructor(val projects: List<Project>)

/**
 * Makes this task out of date when the built `dist/` or the `package.json` of a project in
 * [workspaceDependencyProjects] changes: `dist/` is what tsc, vite and oxlint consume, and the
 * manifest's `exports`/`types` mapping can remap resolution without changing it.
 */
fun Task.declareWorkspaceDependencyInputs(workspaceDependencyProjects: WorkspaceDependencyProjects) {
  inputs.files(workspaceDependencyProjects.projects.map { dependencyProject -> dependencyProject.fileTree(dependencyProject.file("dist")) })
    .withPropertyName("workspaceDependencyDist").withPathSensitivity(PathSensitivity.RELATIVE)
  inputs.files(workspaceDependencyProjects.projects.map { dependencyProject -> dependencyProject.file("package.json") })
    .withPropertyName("workspaceDependencyManifests").withPathSensitivity(PathSensitivity.RELATIVE)
}
