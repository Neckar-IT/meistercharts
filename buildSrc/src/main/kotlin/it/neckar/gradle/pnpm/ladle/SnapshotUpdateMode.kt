package it.neckar.gradle.pnpm.ladle

import org.gradle.api.GradleException

/**
 * The values of Playwright's `--update-snapshots`, set through `-Pplaywright.updateSnapshots=<value>`.
 */
internal enum class SnapshotUpdateMode(val cliValue: String) {
  /** Rewrites every baseline into an emptied directory, so a baseline without a story disappears. */
  All("all"),

  /** Rewrites the baselines that differ, writes the missing ones. */
  Changed("changed"),

  /** Writes the missing baselines only — Playwright's default. */
  Missing("missing"),

  /** Writes no baseline. */
  None("none"),
  ;

  companion object {
    fun parse(value: String): SnapshotUpdateMode {
      return entries.firstOrNull { it.cliValue == value }
        ?: throw GradleException("-P${LadleTasks.UpdateSnapshotsProperty}=$value is unknown, expected one of ${entries.joinToString { it.cliValue }}")
    }
  }
}
