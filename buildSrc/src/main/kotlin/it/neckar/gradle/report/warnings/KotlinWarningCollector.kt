package it.neckar.gradle.report.warnings

import java.util.concurrent.ConcurrentHashMap

/**
 * Collects the warnings of one build, kept apart by the compilation that reported them.
 *
 * Per task, because that is the unit the findings are published as: one fragment per compilation, written
 * when that compilation finishes. Across tasks, [collected] merges what belongs together — a `commonMain`
 * diagnostic reaches `compileKotlinJvm`, `compileKotlinJs` and `compileKotlinMetadata` separately, same
 * file, same position, same message. They are one finding, and the targets that reported it are kept so
 * the merge loses nothing.
 *
 * Thread-safe: compile tasks run in parallel and their output arrives on many threads.
 */
class KotlinWarningCollector {

  private val warningsByTask = ConcurrentHashMap<String, MutableList<KotlinWarning>>()

  fun record(taskPath: String, warning: KotlinWarning) {
    val warnings = warningsByTask.computeIfAbsent(taskPath) { mutableListOf() }
    synchronized(warnings) { warnings.add(warning) }
  }

  /**
   * What one compilation reported, ordered by location. One compilation is one target, so nothing is
   * merged here — only an exact repeat of the same diagnostic is dropped.
   */
  fun warningsOf(taskPath: String): List<KotlinWarning> {
    val warnings = warningsByTask[taskPath] ?: return emptyList()

    return synchronized(warnings) { warnings.toList() }
      .distinctBy { it.deduplicationKey }
      .sortedInReportOrder()
  }

  /** Every finding of this build, merged across the targets that reported it. */
  fun collected(): List<KotlinWarning> {
    val merged = LinkedHashMap<String, KotlinWarning>()

    warningsByTask.values.forEach { warnings ->
      synchronized(warnings) { warnings.toList() }.forEach { warning ->
        merged.merge(warning.deduplicationKey, warning) { existing, added ->
          existing.copy(targets = existing.targets + added.targets)
        }
      }
    }

    return merged.values.sortedInReportOrder()
  }
}

/** A stable order keeps every report diffable between builds. */
private fun Iterable<KotlinWarning>.sortedInReportOrder(): List<KotlinWarning> =
  sortedWith(compareBy({ it.filePath }, { it.line }, { it.column }, { it.message }))
