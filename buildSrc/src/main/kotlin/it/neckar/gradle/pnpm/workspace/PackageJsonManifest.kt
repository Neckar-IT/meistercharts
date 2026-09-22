package it.neckar.gradle.pnpm.workspace

import it.neckar.projects.GradleProjectPath
import it.neckar.projects.RepositoryPath

data class PackageJsonManifest(
  val module: GradleProjectPath,
  val path: RepositoryPath,
  val text: String,
)
