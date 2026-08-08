package it.neckar.gradle.report.warnings

import it.neckar.gradle.report.events.TaskOperationResolver
import org.gradle.api.internal.tasks.execution.ExecuteTaskBuildOperationType
import org.gradle.api.logging.LogLevel
import org.gradle.internal.logging.events.operations.LogEventBuildOperationProgressDetails
import org.gradle.internal.logging.events.operations.StyledTextBuildOperationProgressDetails
import org.gradle.internal.operations.BuildOperationDescriptor
import org.gradle.internal.operations.BuildOperationListener
import org.gradle.internal.operations.BuildOperationListenerManager
import org.gradle.internal.operations.OperationFinishEvent
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.operations.OperationProgressEvent
import org.gradle.internal.operations.OperationStartEvent
import java.io.File

/**
 * Collects the Kotlin compiler's warnings during the build, attributed to the task that produced them.
 *
 * Sibling of [it.neckar.gradle.report.events.BuildTaskOutputCapture]: same mechanism (build-operation
 * progress notifications, the only thing that attributes output to tasks under parallel execution),
 * different purpose. It reads the same events rather than the log files that capture writes — the
 * level, the task and therefore the module are fields here, and re-parsing them out of a file path
 * afterwards would only lose information.
 *
 * Which output can hold a diagnostic is decided structurally: console output of a
 * `compile[<Scope>]Kotlin[<Target>]` task. The event's `category` cannot help — every task's output
 * arrives as `org.gradle.api.Task` — but the task path is already resolved and yields the module too.
 *
 * The severity comes from the compiler's own `w:` prefix, not from the event's log level: an
 * out-of-process compiler's output is levelled by the stream it came from, so a Kotlin/Native warning
 * arrives as ERROR (see `diagnosticTextOrNull`). [KotlinWarningParser] requires the prefix and drops
 * everything else, including `e:` errors.
 *
 * Position, file and message are parsed out of the text because the Kotlin Gradle plugin exposes no
 * structured diagnostic channel — they exist solely there.
 *
 * The collection covers only tasks that actually run. An `UP-TO-DATE` or `FROM-CACHE` compilation emits
 * nothing, so what this build reports is always "what it compiled", never a repository-wide total. The
 * fragments on disk accumulate across builds and do hold that total — see [KotlinWarningFragments].
 */
class KotlinWarningCapture(
  private val repositoryRoot: File,
  /** Where the per-compilation fragments are written. See [KotlinWarningFragments]. */
  private val fragmentDirectory: File,
  private val buildOperationListenerManager: BuildOperationListenerManager,
) {

  private val resolver = TaskOperationResolver()
  private val collector = KotlinWarningCollector()

  private val operationListener = object : BuildOperationListener {
    override fun started(descriptor: BuildOperationDescriptor, startEvent: OperationStartEvent) {
      val taskPath = (descriptor.details as? ExecuteTaskBuildOperationType.Details)?.taskPath
      resolver.record(descriptor.id?.id ?: return, descriptor.parentId?.id, taskPath)
    }

    override fun progress(operationIdentifier: OperationIdentifier, progressEvent: OperationProgressEvent) {
      val text = progressEvent.details?.diagnosticTextOrNull() ?: return
      val taskPath = resolver.resolveTaskPath(operationIdentifier.id) ?: return
      val target = KotlinCompileTask.targetOrNull(taskPath) ?: return

      text.lineSequence().forEach { line ->
        KotlinWarningParser.parse(line, taskPath.modulePath(), target, repositoryRoot)?.let { collector.record(taskPath, it) }
      }
    }

    /**
     * Publishes the finished compilation's findings. A task that did not run the compiler leaves its
     * fragment untouched — its `skipMessage` names why (`UP-TO-DATE`, `FROM-CACHE`), and overwriting the
     * fragment with the nothing it reported would lose the last real result.
     *
     * A compilation that ran and found nothing writes an empty fragment, which is how a cleaned-up module
     * disappears from the report.
     */
    override fun finished(descriptor: BuildOperationDescriptor, finishEvent: OperationFinishEvent) {
      val taskPath = (descriptor.details as? ExecuteTaskBuildOperationType.Details)?.taskPath ?: return
      KotlinCompileTask.targetOrNull(taskPath) ?: return
      if ((finishEvent.result as? ExecuteTaskBuildOperationType.Result)?.skipMessage != null) return

      KotlinWarningFragments.write(fragmentDirectory, taskPath, collector.warningsOf(taskPath))
    }
  }

  /** Every collected finding, ordered by location so the report is stable across builds. */
  fun collectedWarnings(): List<KotlinWarning> = collector.collected()

  fun install() {
    buildOperationListenerManager.addListener(operationListener)
  }

  /**
   * Removes the listener. Mandatory: the listener manager is daemon-scoped, so one left behind keeps
   * firing into a dead capture on every later build in the same daemon.
   */
  fun uninstall() {
    buildOperationListenerManager.removeListener(operationListener)
  }
}

/** `:internal:open:commons:app:compileKotlinJvm` -> `:internal:open:commons:app`. */
private fun String.modulePath(): String = substringBeforeLast(':', missingDelimiterValue = "")

/**
 * The diagnostic text a build-operation progress detail contributes, or null when it carries none.
 *
 * Both detail types have to be handled, for the same reason
 * [it.neckar.gradle.report.events.BuildTaskOutputCapture] handles both: the JVM and JS compilations
 * log through the Gradle logger and arrive as [LogEventBuildOperationProgressDetails], while the
 * Kotlin/Native compiler runs out of process and its console output is captured as
 * [StyledTextBuildOperationProgressDetails]. Styled text arrives in spans that carry their own
 * newlines, so one detail can hold several diagnostics — the caller splits into lines.
 *
 * **The level names the stream, not the severity.** A Kotlin/Native warning arrives at
 * [LogLevel.ERROR] because the compiler writes it to stderr and Gradle maps captured stderr to ERROR
 * regardless of content — verified by probing the listener during a `compileTestKotlinLinuxX64` run.
 * Filtering on `WARN` therefore dropped every native warning. What actually carries the severity is
 * the compiler's own `w:` / `e:` prefix, which [KotlinWarningParser] already requires, so errors are
 * excluded there rather than here.
 *
 * [LogLevel.INFO] and below stay out: that is task progress, not compiler output.
 */
private fun Any.diagnosticTextOrNull(): String? = when (this) {
  is LogEventBuildOperationProgressDetails -> message?.takeIf { logLevel.carriesCompilerDiagnostics() }

  is StyledTextBuildOperationProgressDetails ->
    if (logLevel.carriesCompilerDiagnostics()) spans.joinToString(separator = "") { it.text } else null

  else -> null
}

private fun LogLevel.carriesCompilerDiagnostics(): Boolean = this == LogLevel.WARN || this == LogLevel.ERROR
