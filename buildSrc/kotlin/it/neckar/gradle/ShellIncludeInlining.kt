package it.neckar.gradle

import it.neckar.projects.Projects
import org.gradle.api.tasks.AbstractCopyTask

/**
 * Inlines shared shell snippets into the scripts of an [AbstractCopyTask] during
 * materialization, so the finished script is self-contained and the included
 * content is visibly delimited.
 *
 * A script opts in with a single marker line:
 * ```
 * # @inline: host-provisioning/provision-lib.sh
 * ```
 * The path is relative to `:internal:infrastructure:common`. The marker line is
 * replaced by the referenced file's content (shebang dropped), wrapped in
 * clearly visible `BEGIN INCLUDE` / `END INCLUDE` comments:
 * ```
 * # ======================== BEGIN INCLUDE: host-provisioning/provision-lib.sh ========================
 * …file content…
 * # ======================== END INCLUDE: host-provisioning/provision-lib.sh ========================
 * ```
 *
 * The single source of truth stays the file under `common/`; materialization
 * inlines a copy. Scripts that use a marker are always run materialized (from the
 * build directory), never from source.
 */
fun AbstractCopyTask.inlineCommonShellIncludes() {
  val commonDir = Projects.infrastructure_common.project().projectDir

  // The inlined library files are not in the task's `from(...)` set, so declare them
  // as inputs explicitly — otherwise editing a `*-lib.sh` would not re-materialize the
  // scripts that inline it (the task would stay up-to-date on its source dir alone).
  // Listed as concrete files (not a tree rooted at `common/`, which would overlap the
  // build outputs of common subprojects and trip Gradle's implicit-dependency check).
  inputs.files(InlinableLibraries.map { commonDir.resolve(it) }.filter { it.isFile })
    .withPropertyName("inlinedCommonShellIncludes")

  doFirst {
    filter { line ->
      val match = InlineMarkerRegex.matchEntire(line) ?: return@filter line

      val indent = match.groupValues[1]
      val relativePath = match.groupValues[2]

      val includeFile = commonDir.resolve(relativePath)
      require(includeFile.isFile) {
        "Inline include not found: ${includeFile.absolutePath} (referenced by `# @inline: $relativePath`)"
      }

      val body = includeFile.readText()
        .lineSequence()
        .dropWhile { it.startsWith("#!") }
        .joinToString("\n")
        .trim('\n')

      buildString {
        append(indent).append(InlineBeginMarker).append(relativePath).append(InlineMarkerTail).append('\n')
        append(body).append('\n')
        append(indent).append(InlineEndMarker).append(relativePath).append(InlineMarkerTail)
      }
    }
  }
}

/**
 * The shared shell libraries that may be inlined via `# @inline:`, relative to
 * `:internal:infrastructure:common`. Declared as task inputs so editing one
 * re-materializes the scripts that inline it. Add new libraries here.
 */
private val InlinableLibraries: List<String> = listOf(
  "host-provisioning/provision-lib.sh",
  "host-deploy/deploy-host-lib.sh",
  "service-deploy/deploy-service-lib.sh",
)

private val InlineMarkerRegex = Regex("""^(\s*)#\s*@inline:\s*(\S+)\s*$""")

private const val InlineBeginMarker: String =
  "# ======================== BEGIN INCLUDE: "
private const val InlineEndMarker: String =
  "# ======================== END INCLUDE: "
private const val InlineMarkerTail: String =
  " ========================"
