package it.neckar.gradle.pnpm.workspace

import it.neckar.gradle.pnpm.dependency.NpmPackageName

data class PnpmDependencyEdge(
  val dependent: PnpmDependent,
  val dependencyName: NpmPackageName,
  val resolution: PnpmResolution,
) {
  /** Whether this entry installs the copy that [snapshotKey] names. */
  fun resolvesTo(snapshotKey: PnpmSnapshotKey): Boolean {
    return dependencyName == snapshotKey.packageName && resolution == snapshotKey.resolution
  }
}
