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
 * The running artifact's identity is deploy metadata, never a compile input: a container receives
 * [imageDigestOrNull] from the deploy; a fat-jar CLI and a lizergy frontend, whose deploy sets it, carry [gitHash].
 */
object VersionInformation {
  /**
   * Resolver result when no injection source provides a value (local development, plain tests).
   */
  const val UnknownValue: String = "unknown"

  /**
   * Length of [gitHashShort] and of the hex part of [ImageDigestText.shortOf].
   */
  const val ShortHashLength: Int = 12

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
   * Resolved at runtime from the injected deploy metadata (see [resolveGitProperty]).
   */
  val gitCommitDateTime: String by lazy { resolveGitProperty(GitProperty.CommitDateTime) }

  /**
   * The git commit date and time as [Instant] — null when unresolved ([UnknownValue]),
   * e.g. in local development where no deploy metadata is injected.
   */
  val gitCommitInstantOrNull: Instant?
    get() = gitCommitDateTime.takeUnless { it == UnknownValue }?.let { Instant.parse(it) }

  /**
   * The git hash of the current commit.
   * Resolved at runtime from the injected deploy metadata (see [resolveGitProperty]).
   */
  val gitHash: String by lazy { resolveGitProperty(GitProperty.Hash) }

  /**
   * The git hash of the current commit — null when unresolved ([UnknownValue]),
   * e.g. in local development where no deploy metadata is injected.
   */
  val gitHashOrNull: String?
    get() = gitHash.takeUnless { it == UnknownValue }

  /**
   * The short git hash of the current commit — derived from [gitHash] (single source, no drift).
   * [UnknownValue] is shorter than [ShortHashLength] and passes through unchanged.
   */
  val gitHashShort: String
    get() = gitHash.take(ShortHashLength)

  /**
   * Null where the deploy injects no digest or [UnpinnedImageDigest]. On a malformed value the first access throws, which a service
   * makes at start; a page reads the value as not injected (see [resolveImageDigest]).
   */
  val imageDigestOrNull: String? by lazy { pinnedImageDigestOrNull(resolveImageDigest()) }

  /**
   * Environment variable the deploy sets in the compose service to the pinned image digest.
   */
  const val ImageDigestEnvVar: String = "APP_IMAGE_DIGEST"

  /**
   * Name of the `<meta>` tag a served frontend carries the image digest in.
   */
  const val ImageDigestMetaName: String = "imageDigest"

  /**
   * Value of [ImageDigestEnvVar] when the deploy pinned no digest.
   */
  const val UnpinnedImageDigest: String = "unpinned"

  /**
   * What identifies the running artifact: [imageDigestOrNull] where the deploy injects one, otherwise
   * [gitHash] (a fat-jar CLI, a lizergy frontend whose deploy sets it).
   */
  val artifactIdentity: String
    get() = artifactIdentityOf(imageDigestOrNull, gitHash)

  /**
   * Display form of [artifactIdentity]: the short digest or [gitHashShort].
   */
  val artifactIdentityShort: String
    get() = artifactIdentityShortOf(imageDigestOrNull, gitHash)

  /**
   * Verbose version string that contains [artifactIdentity]
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($artifactIdentity)"
    }

  /**
   * Compact version string for UI display: commit date (no time) plus the short hash,
   * e.g. `2026-07-15 - 1d79fb128769`. See [formatCommitDateAndShortHash].
   *
   * [version] is deliberately absent: it is identical across all services and changes only per
   * release, so it says nothing about which state is deployed.
   */
  val commitDateAndShortHash: String
    get() = formatCommitDateAndShortHash(gitCommitDateTime, gitHashShort)
}

/**
 * A malformed value is a broken deploy and throws.
 */
internal fun pinnedImageDigestOrNull(injected: String?): String? {
  return when {
    injected == null || injected == VersionInformation.UnpinnedImageDigest -> null
    ImageDigestText.isWellFormed(injected) -> injected
    else -> error("${VersionInformation.ImageDigestEnvVar} must be ${ImageDigestText.WellFormedDescription} or ${VersionInformation.UnpinnedImageDigest} but was <$injected>")
  }
}

internal fun artifactIdentityOf(imageDigest: String?, gitHash: String): String {
  return imageDigest ?: gitHash
}

/**
 * The display form of [artifactIdentityOf]: the short digest, otherwise the short git hash.
 */
internal fun artifactIdentityShortOf(imageDigest: String?, gitHash: String): String {
  return ImageDigestText.shortOrElse(imageDigest) { gitHash.take(VersionInformation.ShortHashLength) }
}

/**
 * The text form of an image digest, shared by [VersionInformation] and the REST value class `ImageDigest`.
 */
object ImageDigestText {
  /** The only digest algorithm the registries in use produce. */
  const val Algorithm: String = "sha256:"

  /** The well-formed shape, as error messages name it. */
  const val WellFormedDescription: String = "$Algorithm<64 hex characters>"

  private val Pattern: Regex = Regex("^" + Regex.escape(Algorithm) + "[a-f0-9]{64}$")

  /**
   * True for [Algorithm] followed by 64 lowercase hex characters.
   */
  fun isWellFormed(value: String): Boolean {
    return Pattern.matches(value)
  }

  /**
   * Throws unless [value] [isWellFormed].
   */
  fun requireWellFormed(value: String) {
    require(isWellFormed(value)) { "Image digest must be $WellFormedDescription but was <$value>" }
  }

  /**
   * The first [VersionInformation.ShortHashLength] hex characters behind [Algorithm] of the well-formed [digest], for display.
   */
  fun shortOf(digest: String): String {
    requireWellFormed(digest)
    return digest.substring(Algorithm.length, Algorithm.length + VersionInformation.ShortHashLength)
  }

  /**
   * [shortOf] of [digest] where there is one, otherwise [fallback] — the display rule "the digest names the artifact first".
   */
  fun shortOrElse(digest: String?, fallback: () -> String): String {
    return digest?.let { shortOf(it) } ?: fallback()
  }
}

/**
 * Formats the commit date (the date component of [commitDateTime]) and [gitHashShort] as
 * `2026-07-15 - 1d79fb128769`.
 *
 * The verbose form (ISO timestamp plus the 40-character hash, ~66 characters) blows up narrow
 * layouts: the Lizergy sidebar's position-fixed footer grew past the sidebar column and
 * intercepted clicks on the main content, which turned the nightly Playwright run red.
 *
 * A component that is not resolvable contributes [VersionInformation.UnknownValue]; when
 * neither resolves, the result is that value alone. A build without deploy metadata must stay
 * recognizable as such instead of rendering a date and a hash nobody can look up.
 */
internal fun formatCommitDateAndShortHash(commitDateTime: String, gitHashShort: String): String {
  val commitDate = commitDateTime.takeUnless { it == VersionInformation.UnknownValue }?.substringBefore('T')
  val hash = gitHashShort.takeUnless { it == VersionInformation.UnknownValue }

  if (commitDate == null && hash == null) {
    return VersionInformation.UnknownValue
  }

  return "${commitDate ?: VersionInformation.UnknownValue} - ${hash ?: VersionInformation.UnknownValue}"
}

/**
 * Resolves the value of a single git property from the injected deploy metadata at runtime.
 *
 * JVM chain: system property ([GitProperty.systemProperty]) → environment variable
 * ([GitProperty.envVar], set by a compose file that names the deployed commit) → the
 * `META-INF/app-git-info.properties` resource ([GitProperty.propertyKey] as key, packed
 * exclusively into leaf fat-jars) → [VersionInformation.UnknownValue].
 *
 * JS chain: `window.__APP_GIT_INFO__[propertyKey]` (filled at serve time) → `<meta>` of the same
 * name → [VersionInformation.UnknownValue]. Non-browser runtimes (Node, tests) resolve to the
 * fallback without throwing.
 *
 * The [GitProperty] enum is generated 1:1 from the GitProperty enum in build-logic
 * ResourcesExt.kt (single source of truth), so injection and resolution cannot drift.
 */
internal expect fun resolveGitProperty(property: GitProperty): String

/**
 * Resolves the injected image digest: JVM from [VersionInformation.ImageDigestEnvVar], JS from the
 * `<meta>` named [VersionInformation.ImageDigestMetaName]. Null when absent, empty or an unreplaced
 * placeholder; on JS also for a malformed value.
 */
internal expect fun resolveImageDigest(): String?

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
