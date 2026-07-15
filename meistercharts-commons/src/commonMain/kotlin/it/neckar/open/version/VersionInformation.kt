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
package it.neckar.open.version

import kotlin.time.Instant

/**
 * Application related information.
 *
 * The stable value (version) comes from generated [VersionConstants].
 * Git info is a deploy metadatum, not a compile input (#2413): it is injected at the artifact
 * edges (service image env, fat-jar resource, serve-time HTML) and resolved from there at
 * runtime. No compiled artifact carries it, so builds stay a pure function of the sources.
 * Where nothing is injected (local development, plain tests) the git values are [UnknownGitValue].
 */
object VersionInformation {
  /**
   * Resolver result when no injection source provides a value (local development, plain tests).
   */
  const val UnknownGitValue: String = "unknown"

  /**
   * Length of [gitHashShort], derived from [gitHash].
   */
  private const val ShortHashLength: Int = 12

  /**
   * The version number (main version number of the repository)
   */
  val version: String = VersionConstants.monorepoVersion

  /**
   * Returns true if the current version is a snapshot
   */
  val isSnapshot: Boolean = version.isSnapshot()

  /**
   * The git commit date and time (ISO 8601 format).
   * Resolved at runtime from the injected deploy metadata (see [resolveGitInfo]).
   */
  val gitCommitDateTime: String by lazy { resolveGitInfo(GitProperty.CommitDateTime) }

  /**
   * The git commit date and time as [Instant] — null when unresolved ([UnknownGitValue]),
   * e.g. in local development where no deploy metadata is injected.
   */
  val gitCommitInstantOrNull: Instant?
    get() = gitCommitDateTime.takeUnless { it == UnknownGitValue }?.let { Instant.parse(it) }

  /**
   * The git hash of the current commit.
   * Resolved at runtime from the injected deploy metadata (see [resolveGitInfo]).
   */
  val gitHash: String by lazy { resolveGitInfo(GitProperty.Hash) }

  /**
   * The git hash of the current commit — null when unresolved ([UnknownGitValue]),
   * e.g. in local development where no deploy metadata is injected.
   */
  val gitHashOrNull: String?
    get() = gitHash.takeUnless { it == UnknownGitValue }

  /**
   * The short git hash of the current commit — derived from [gitHash] (single source, no drift).
   * [UnknownGitValue] is shorter than [ShortHashLength] and passes through unchanged.
   */
  val gitHashShort: String
    get() = gitHash.take(ShortHashLength)

  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}

/**
 * Resolves a git property from the injected deploy metadata at runtime.
 *
 * JVM chain: system property ([GitProperty.systemProperty]) → environment variable
 * ([GitProperty.envVar], baked into service images at image build) → the
 * `META-INF/app-git-info.properties` resource ([GitProperty.propertyKey] as key, packed
 * exclusively into leaf fat-jars) → [VersionInformation.UnknownGitValue].
 *
 * JS chain: `window.__APP_GIT_INFO__[propertyKey]` (filled at serve time) → `<meta
 * name="app-version">` (hash only) → [VersionInformation.UnknownGitValue]. Non-browser runtimes
 * (Node, tests) resolve to the fallback without throwing.
 *
 * The [GitProperty] enum is generated 1:1 from the GitProperty enum in build-logic
 * ResourcesExt.kt (single source of truth), so injection and resolution cannot drift.
 */
internal expect fun resolveGitInfo(property: GitProperty): String

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
