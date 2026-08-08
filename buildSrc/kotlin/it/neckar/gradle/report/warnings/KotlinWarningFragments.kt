package it.neckar.gradle.report.warnings

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The findings on disk: one JSON fragment per Kotlin compilation, under
 * `build-reports/kotlin-warnings/<module path>/<task>.json`.
 *
 * ```
 * build-reports/kotlin-warnings/
 *   internal/open/commons/kotlin-collections/compileKotlinJvm.json
 *   internal/open/commons/kotlin-collections/compileKotlinJs.json
 *   internal/patterns/ktor-backend/compileKotlin.json
 * ```
 *
 * The directory below `kotlin-warnings/` is the module's own path, so a fragment left behind by a deleted
 * module is recognisable as such — `tools/cleanups/scripts/orphaned-kotlin-warning-fragments.sh` removes
 * those, because a module that is gone never compiles again and never clears its own fragment.
 *
 * Per compilation, not per build, because that is what the data is — and because it makes the
 * repository-wide stock survive the builds that follow it. A Gradle invocation that compiles nothing
 * writes nothing and truncates nothing, so the fragments of a cold compile run are still there when the
 * report image is packaged two invocations later. A single per-build file would be back to `[]` by then.
 *
 * Kept apart from [KotlinWarningsCodeQualityReport], which renders GitLab's schema and has no place for
 * the module, the reporting target or the diagnostic name as fields of their own.
 */
object KotlinWarningFragments {

  /** Directory under the structured report directory. Every fragment lives below it. */
  const val DirectoryName: String = "kotlin-warnings"

  private val json = Json {
    prettyPrint = true
    // A fragment written by a newer capture must stay readable by an older reader.
    ignoreUnknownKeys = true
  }

  private val serializer = ListSerializer(SerializedKotlinWarning.serializer())

  /**
   * Writes what [taskPath] reported. An empty list writes an empty fragment rather than deleting it — a
   * compilation that ran and found nothing is a result, and it is how a cleaned-up module disappears
   * from the report.
   */
  fun write(fragmentDirectory: File, taskPath: String, warnings: List<KotlinWarning>) {
    val fragmentFile = File(fragmentDirectory, taskPath.toFragmentPath(FragmentFileExtension))
    fragmentFile.parentFile.mkdirs()
    fragmentFile.writeText(render(warnings))
  }

  /**
   * The fragments below [fragmentDirectory]. Empty when the directory does not exist, which is every
   * build that compiled nothing. The single place that decides what counts as a fragment file — the
   * staging task in `internal/closed/reports.neckar.it/build.gradle.kts` mirrors this glob.
   */
  fun fragmentFilesIn(fragmentDirectory: File): List<File> =
    fragmentDirectory.walkTopDown().filter { it.isFile && it.extension == FragmentFileExtension }.toList()

  /** Every finding across the given fragments, merged over the targets that reported it. */
  fun readAll(fragmentFiles: Iterable<File>): List<KotlinWarning> {
    val collector = KotlinWarningCollector()

    fragmentFiles.forEach { fragmentFile ->
      // Names the file: a malformed one among a few hundred is otherwise located by JSON offset alone.
      val warnings = try {
        parse(fragmentFile.readText())
      } catch (e: SerializationException) {
        throw IllegalStateException("Malformed Kotlin warning fragment: $fragmentFile", e)
      }

      warnings.forEach { collector.record(fragmentFile.path, it) }
    }

    return collector.collected()
  }

  const val FragmentFileExtension: String = "json"

  fun render(warnings: List<KotlinWarning>): String =
    json.encodeToString(serializer, warnings.map { it.toSerialized() })

  fun parse(fragmentJson: String): List<KotlinWarning> =
    json.decodeFromString(serializer, fragmentJson).map { it.toWarning() }

  private fun KotlinWarning.toSerialized(): SerializedKotlinWarning = SerializedKotlinWarning(
    filePath = filePath,
    line = line,
    column = column,
    diagnostic = diagnostic,
    message = message,
    modulePath = modulePath,
    targets = targets.sorted(),
  )

  private fun SerializedKotlinWarning.toWarning(): KotlinWarning = KotlinWarning(
    filePath = filePath,
    line = line,
    column = column,
    message = message,
    modulePath = modulePath,
    targets = targets.toSet(),
    diagnostic = diagnostic,
  )
}

/**
 * `:internal:open:commons:kotlin-collections:compileKotlinJvm` ->
 * `internal/open/commons/kotlin-collections/compileKotlinJvm.json`. A task of the root project has no
 * module segment and lands directly in the fragment directory.
 *
 * The module's own path rather than a flattened `internal-open-commons-kotlin-collections`: a module name
 * may contain a dash, so the flattened form cannot be mapped back to a directory, and
 * `tools/cleanups/scripts/orphaned-kotlin-warning-fragments.sh` needs exactly that mapping to tell a
 * fragment of a deleted module from a live one.
 */
private fun String.toFragmentPath(fileExtension: String): String {
  val moduleDirectory = substringBeforeLast(':').trim(':').replace(':', '/')
  val fragmentFileName = "${substringAfterLast(':')}.$fileExtension"

  return if (moduleDirectory.isEmpty()) fragmentFileName else "$moduleDirectory/$fragmentFileName"
}

/**
 * Wire shape of one finding. A list rather than [KotlinWarning.targets]'s set, so the fragment is stable
 * between builds and diffable.
 */
@Serializable
internal data class SerializedKotlinWarning(
  val filePath: String,
  val line: Int,
  val column: Int,
  val diagnostic: String?,
  val message: String,
  val modulePath: String,
  val targets: List<String>,
)
