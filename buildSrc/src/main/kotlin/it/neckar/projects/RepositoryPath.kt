package it.neckar.projects

import kotlinx.serialization.Serializable
import java.io.File
import kotlin.jvm.JvmInline

/**
 * A path inside the repository, relative to its root, with forward slashes:
 * `internal/closed/adl.neckar.it/build.gradle.kts`.
 *
 * What the CI planning and the login guard exports work on — the entries of `git diff --name-only`,
 * a module's directory, the compose file that carries a directive. It is deliberately not a [java.io.File]: nothing
 * here is resolved against a filesystem (a deleted file is in the diff and does not exist), the
 * separator is `/` on every platform because Git produces it, and the questions asked of it are
 * about the repository tree — [isInside], [isInAttic] — not about a file on disk.
 *
 * A directory and a file are the same type: the diff names files, the rules name directories, and
 * every question between the two is [isInside].
 */
@Serializable
@JvmInline
value class RepositoryPath(val value: String) : Comparable<RepositoryPath> {
  init {
    require(value.isNotBlank()) { "Repository path must not be blank" }
    require(value.startsWith("/").not()) { "Repository path is relative to the repository root, so it must not start with '/': $value" }
    require(value.endsWith("/").not()) { "Repository path must not end with '/': $value" }
    require(value.contains('\\').not()) { "Repository path separates with '/', as Git writes it: $value" }
    require(value.split('/').none { it == "." || it == ".." }) { "Repository path is written out, without '.' or '..' segments: $value" }
  }

  /** The last segment: `internal/closed/adl.neckar.it/build.gradle.kts` → `build.gradle.kts`. */
  val fileName: String
    get() = value.substringAfterLast('/')

  /** The first segment: `internal/closed/adl.neckar.it` → `internal`; a path at the root is its own. */
  val firstSegment: String
    get() = value.substringBefore('/')

  /** True when this path is [directory] itself or lies below it. */
  fun isInside(directory: RepositoryPath): Boolean {
    return value == directory.value || value.startsWith("${directory.value}/")
  }

  /** True when this path lies strictly below [directory] — the directory itself is not inside itself. */
  fun isStrictlyInside(directory: RepositoryPath): Boolean {
    return value.startsWith("${directory.value}/")
  }

  /**
   * True when a directory on the way to this path is named [AtticDirectoryName]: `internal/attic/deger/build.gradle.kts`,
   * `docs/attic/notes.adoc`, `attic/x`. The attic holds retired code.
   */
  fun isInAttic(): Boolean {
    return value.split('/').dropLast(1).contains(AtticDirectoryName)
  }

  /**
   * The first segment below [directory]: `internal/infrastructure/common/traefik/compose.yml` below
   * `internal/infrastructure/common` is `traefik`. Null when this path is not inside [directory] —
   * and for the directory itself, which has no segment below it.
   */
  fun firstSegmentBelow(directory: RepositoryPath): String? {
    if (isStrictlyInside(directory).not()) {
      return null
    }
    return value.removePrefix("${directory.value}/").substringBefore('/')
  }

  /** True when the path ends with [suffix] — a file-name suffix such as `.md` or `.gradle.kts`. */
  fun endsWith(suffix: String): Boolean {
    return value.endsWith(suffix)
  }

  /** The path of [segment] below this one: `internal/closed` + `adl.neckar.it` → `internal/closed/adl.neckar.it`. */
  fun resolve(segment: String): RepositoryPath {
    require(segment.isNotBlank()) { "Segment must not be blank" }
    return RepositoryPath("$value/$segment")
  }

  override fun toString(): String {
    return value
  }

  /** Paths are listed to humans and diffed between pipeline runs, so they sort by their text. */
  override fun compareTo(other: RepositoryPath): Int {
    return value.compareTo(other.value)
  }

  companion object {
    /** The directory name that marks retired code at any depth of the repository — see [isInAttic]. */
    const val AtticDirectoryName: String = "attic"

    /**
     * Drops every path [isInAttic] matches from a `git ls-files` or `git grep`. It goes first, directly after `--`:
     * behind some pathspecs (`internal/`, `tools/java`, a wildcard below a directory) git 2.54 lists nothing or ignores the exclude.
     */
    const val AtticExcludePathspec: String = ":(exclude,glob)**/$AtticDirectoryName/**"

    /**
     * The path of [file] below [repositoryRoot] — the boundary where a file on disk becomes a path in
     * the repository, with the separator Git writes whatever the platform uses.
     */
    fun of(file: File, repositoryRoot: File): RepositoryPath {
      return RepositoryPath(file.relativeTo(repositoryRoot).invariantSeparatorsPath)
    }
  }
}
