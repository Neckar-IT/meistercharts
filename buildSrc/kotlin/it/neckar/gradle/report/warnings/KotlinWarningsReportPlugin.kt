package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.structuredReportDirectory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.internal.GradleInternal
import org.gradle.internal.operations.BuildOperationListenerManager

/**
 * Publishes the Kotlin compiler warnings, in two places with different lifetimes. Apply once, to the root
 * project.
 *
 * | Artefact | Written | Consumer |
 * |---|---|---|
 * | `build-reports/kotlin-warnings/<module>/<task>.json` | per compilation, as it finishes | the `Kotlin Compiler Warnings` card on reports.neckar.it |
 * | `build-reports/kotlin-warnings.json` | at build end, GitLab Code Quality schema | `gitlab-ci.d/mr.yml`, inline annotations in the MR diff |
 *
 * The split follows the two questions being asked. The Code Quality report answers "what did *this* build
 * compile" and is rewritten every time, which is right for a diff-scoped merge request pipeline. The
 * fragments answer "what is in the repository": they are written only by compilations that actually ran, so
 * the Gradle invocations that follow a cold compile run leave them alone and the stock survives until the
 * report image is packaged. See [KotlinWarningFragments].
 *
 * Collection runs unconditionally, because a mode would make the report untrustworthy: the capture only
 * sees compilations that actually run, so a flag-gated build with a warm cache would report almost nothing
 * and look like a clean repository.
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
      fragmentDirectory = target.structuredReportDirectory.dir(KotlinWarningFragments.DirectoryName).asFile,
      buildOperationListenerManager = gradle.services.get(BuildOperationListenerManager::class.java),
    )
    capture.install()

    val codeQualityReportFile = target.structuredReportDirectory.file(CodeQualityReportFileName).asFile

    // The listener is daemon-scoped and has to be removed at build end, or it keeps firing into a
    // dead capture on every later build in the same daemon. buildFinished is not
    // configuration-cache-safe, which is consistent with this plugin's overall incompatibility.
    gradle.buildFinished {
      capture.uninstall()

      codeQualityReportFile.parentFile.mkdirs()
      // Written even when empty: the `jq -s` merge in gitlab-ci.d/mr.yml reads the file
      // unconditionally, and a missing one would fail the Code Quality report of the whole pipeline.
      codeQualityReportFile.writeText(KotlinWarningsCodeQualityReport.render(capture.collectedWarnings()))
    }
  }

  companion object {
    /** Sits next to `build-events.jsonl` and `artifact-sizes.json` in the structured report directory. */
    const val CodeQualityReportFileName: String = "kotlin-warnings.json"
  }
}
