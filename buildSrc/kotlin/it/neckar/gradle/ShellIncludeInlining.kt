package it.neckar.gradle

import it.neckar.projects.Projects
import org.gradle.api.tasks.AbstractCopyTask
import java.io.File

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
 * **Transitive includes:** an inlined library may itself carry `# @inline:` markers;
 * they are resolved recursively, so a library declares its own dependencies and the
 * consuming script never has to know about them (e.g. `deploy-service-lib.sh` inlines
 * `compose-up/compose-up-lib.sh`, which inlines `docker-lock/docker-lock-lib.sh`, so a
 * service deploy script that inlines the first gets both). A per-expansion `seen` set drops
 * a path already inlined in the same tree, guarding against cycles and diamonds. It starts
 * empty at every marker line, so two markers in one script that reach the same library inline
 * it twice — a script must not repeat what the library it inlines already brings.
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
  inputs.files(
    InlinedLibraryInputs.map { relativePath ->
      commonDir.resolve(relativePath).also {
        // A renamed library that keeps its old entry here would inline fine and never
        // re-materialize, so the hosts would keep deploying the copy made before the rename.
        require(it.isFile) { "Declared inlinable library does not exist: ${it.absolutePath}" }
      }
    }
  ).withPropertyName("inlinedCommonShellIncludes")

  doFirst {
    filter { line ->
      val match = InlineMarkerRegex.matchEntire(line) ?: return@filter line
      expandShellInclude(commonDir, match.groupValues[2], match.groupValues[1], linkedSetOf())
    }
  }
}

/**
 * Inlines [relativePath] (relative to [commonDir]), recursively resolving any `# @inline:`
 * markers in the included body. [indent] is prepended to the BEGIN/END markers; [seen]
 * tracks the paths already inlined in this expansion tree so a cycle or diamond inlines a
 * library at most once.
 */
internal fun expandShellInclude(
  commonDir: File,
  relativePath: String,
  indent: String,
  seen: MutableSet<String>,
): String {
  if (seen.add(relativePath).not()) {
    return "$indent# @inline: $relativePath (already inlined above)"
  }

  val includeFile = commonDir.resolve(relativePath)
  require(includeFile.isFile) {
    "Inline include not found: ${includeFile.absolutePath} (referenced by `# @inline: $relativePath`)"
  }

  val body = includeFile.readText()
    .lineSequence()
    .dropWhile { it.startsWith("#!") }
    .joinToString("\n") { bodyLine ->
      val nested = InlineMarkerRegex.matchEntire(bodyLine)
      if (nested != null) expandShellInclude(commonDir, nested.groupValues[2], nested.groupValues[1], seen)
      else bodyLine
    }
    .trim('\n')

  return buildString {
    append(indent).append(InlineBeginMarker).append(relativePath).append(InlineMarkerTail).append('\n')
    append(body).append('\n')
    append(indent).append(InlineEndMarker).append(relativePath).append(InlineMarkerTail)
  }
}

/**
 * The shared shell snippets reached by an `# @inline:` marker, relative to
 * `:internal:infrastructure:common`. This is the task's input list, not an allowlist:
 * [expandShellInclude] resolves any path under `common/`, so a library missing here still inlines
 * correctly and only leaves the materialization task up-to-date after an edit to it. Add new
 * entries here; `ShellIncludeInliningTest` fails when a marker in the repository names a library
 * this list does not.
 *
 * Most entries are sourced libraries; `gitlab-runner/cleanup-runner-cache.sh` (run by cron on the
 * worker/git hosts) and `host-maintenance/install-maintenance-cron.sh` (piped to the host during
 * its deploy) are standalone scripts inlined into a per-host copy, so the single source under
 * `common/` stays canonical.
 */
internal val InlinedLibraryInputs: List<String> = listOf(
  "secret-masking/secret-masking-lib.sh",
  "docker-lock/docker-lock-lib.sh",
  "compose-up/compose-up-lib.sh",
  "host-provisioning/provision-lib.sh",
  "host-deploy/deploy-host-lib.sh",
  "host-keys/authorized-keys-lib.sh",
  "service-deploy/deploy-service-lib.sh",
  "gitlab-runner/cleanup-runner-cache.sh",
  "host-maintenance/install-maintenance-cron.sh",
  "worker-host/runner-identity-lib.sh",
)

/**
 * Matches an `# @inline: <path>` marker line. Group 1 is the indent, group 2 the path relative to
 * `:internal:infrastructure:common`. Shared with [ShellIncludeGraph], which walks the same markers
 * to answer which `common/` subdirs a module consumes — the two must never drift apart, or the
 * continuous-deploy resolver would miss exactly the includes the inliner folds in.
 */
internal val InlineMarkerRegex = Regex("""^(\s*)#\s*@inline:\s*(\S+)\s*$""")

private const val InlineBeginMarker: String =
  "# ======================== BEGIN INCLUDE: "
private const val InlineEndMarker: String =
  "# ======================== END INCLUDE: "
private const val InlineMarkerTail: String =
  " ========================"
