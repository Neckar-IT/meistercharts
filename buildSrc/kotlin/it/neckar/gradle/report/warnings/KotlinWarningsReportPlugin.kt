package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.structuredReportDirectory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.internal.operations.BuildOperationListenerManager

/**
 * Writes `build-reports/kotlin-warnings.json` — every Kotlin compiler warning this build produced, in
 * the GitLab Code Quality schema, so the findings become inline annotations in the MR diff next to
 * Detekt's and oxlint's. Apply once, to the root project.
 *
 * Collection runs unconditionally, because a mode would make the report untrustworthy: the capture
 * only sees compilations that actually run, so a flag-gated build with a warm cache would report
 * almost nothing and look like a clean repository. The file always states what this build compiled.
 *
 * Delegates to [KotlinWarningCapture], which resolves warnings to tasks through build operations —
 * see that class for why the task's own logger cannot do it. Reaching into Gradle's internal service
 * registry for [BuildOperationListenerManager] is what that costs; there is no public API that
 * attributes task output. Consequently this plugin is not configuration-cache-compatible, exactly
 * like [it.neckar.gradle.report.events.TaskOutputLogPlugin]. The repo runs with
 * `org.gradle.configuration-cache=false` (#482).
 */
class KotlinWarningsReportPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val gradle = target.gradle as GradleInternal

    val capture = KotlinWarningCapture(
      repositoryRoot = target.rootDir,
      buildOperationListenerManager = gradle.services.get(BuildOperationListenerManager::class.java),
    )
    capture.install()

    val reportFile = target.structuredReportDirectory.file(ReportFileName).asFile

    // The listener is daemon-scoped and has to be removed at build end, or it keeps firing into a
    // dead capture on every later build in the same daemon. buildFinished is not
    // configuration-cache-safe, which is consistent with this plugin's overall incompatibility.
    gradle.buildFinished {
      capture.uninstall()

      reportFile.parentFile.mkdirs()
      // Written even when empty: the `jq -s` merge in gitlab-ci.d/mr.yml reads the file
      // unconditionally, and a missing one would fail the Code Quality report of the whole pipeline.
      reportFile.writeText(KotlinWarningsCodeQualityReport.render(capture.collectedWarnings()))
    }
  }

  companion object {
    /** Sits next to `build-events.jsonl` and `artifact-sizes.json` in the structured report directory. */
    const val ReportFileName: String = "kotlin-warnings.json"
  }
}
