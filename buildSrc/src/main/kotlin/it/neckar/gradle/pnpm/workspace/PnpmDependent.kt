package it.neckar.gradle.pnpm.workspace

/**
 * What declares a dependency in `pnpm-lock.yaml`: a workspace package in `importers:` or an installed
 * package in `snapshots:`.
 */
sealed interface PnpmDependent {
  /** A workspace package, named by its directory relative to the workspace root; `.` is the root itself. */
  data class Importer(val directory: String) : PnpmDependent {
    override fun toString(): String {
      return if (directory == RootDirectory) "$RootDirectory (workspace root)" else directory
    }

    companion object {
      const val RootDirectory: String = "."
    }
  }

  data class Snapshot(val key: PnpmSnapshotKey) : PnpmDependent {
    override fun toString(): String {
      return key.toString()
    }
  }
}
