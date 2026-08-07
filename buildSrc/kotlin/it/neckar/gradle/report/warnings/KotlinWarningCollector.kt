package it.neckar.gradle.report.warnings

import java.util.concurrent.ConcurrentHashMap

/**
 * Collects the warnings of one build, merging the copies the individual targets report.
 *
 * A `commonMain` diagnostic reaches `compileKotlinJvm`, `compileKotlinJs` and `compileKotlinMetadata`
 * separately — same file, same position, same message. They are one finding, and the targets that
 * reported it are kept so the merge loses nothing.
 *
 * Thread-safe: compile tasks run in parallel and their output arrives on many threads.
 */
class KotlinWarningCollector {

  private val warningsByLocation = ConcurrentHashMap<String, KotlinWarning>()

  fun record(warning: KotlinWarning) {
    warningsByLocation.merge(warning.deduplicationKey, warning) { existing, added ->
      existing.copy(targets = existing.targets + added.targets)
    }
  }

  /** Every collected finding, ordered by location so the report is stable across builds. */
  fun collected(): List<KotlinWarning> =
    warningsByLocation.values.sortedWith(compareBy({ it.filePath }, { it.line }, { it.column }, { it.message }))
}
