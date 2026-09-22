package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName
import it.neckar.projects.GradleProjectPath
import it.neckar.projects.RepositoryPath

/** A module declaring a package reserved for [consumer]. */
data class ForeignConsumerDeclaration(
  val packageJson: RepositoryPath,
  /** 1-based */
  val line: Int,
  val module: GradleProjectPath,
  val packageName: NpmPackageName,
  val consumer: GradleProjectPath,
) {
  override fun toString(): String {
    return "$packageJson:$line: $module declares '$packageName', which only $consumer may declare"
  }
}
