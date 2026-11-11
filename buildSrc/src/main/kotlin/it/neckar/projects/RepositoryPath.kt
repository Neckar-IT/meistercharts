/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.projects

import kotlinx.serialization.Serializable
import java.io.File
import kotlin.jvm.JvmInline

/**
 * A path inside the repository, relative to its root, with forward slashes:
 * `internal/open/detekt/build.gradle.kts`.
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

  /** The last segment: `internal/open/detekt/build.gradle.kts` → `build.gradle.kts`. */
  val fileName: String
    get() = value.substringAfterLast('/')

  /** The first segment: `internal/open/detekt` → `internal`; a path at the root is its own. */
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

  /** The path of [segment] below this one: `internal/open` + `detekt` → `internal/open/detekt`. */
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
