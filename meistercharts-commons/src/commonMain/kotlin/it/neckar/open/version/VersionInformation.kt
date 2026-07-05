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

/**
 * Application related information.
 *
 * The stable value (version) comes from generated [VersionConstants].
 * Volatile git info is loaded at runtime from resources (JVM)
 * or returns "unknown" (JS) to avoid unnecessary Kotlin recompilation on every commit.
 */
object VersionInformation {
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
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitCommitDateTime: String by lazy { resolveGitInfo(GitProperty.CommitDateTime) }

  /**
   * The git hash of the current commit.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitHash: String by lazy { resolveGitInfo(GitProperty.Hash) }

  /**
   * The short git hash of the current commit.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val gitHashShort: String by lazy { resolveGitInfo(GitProperty.HashShort) }

  /**
   * The git branch name.
   * Loaded at runtime from git.properties (JVM). Returns "unknown" on JS.
   */
  val branch: String by lazy { resolveGitInfo(GitProperty.Branch) }

  /**
   * Verbose version string that contains the git information
   */
  val versionAsStringVerbose: String
    get() {
      return "$version ($gitHash)"
    }
}

/**
 * Resolves a volatile git property at runtime.
 * On JVM, reads from git.properties resource file.
 * On JS, returns "unknown" (no synchronous resource loading in ESM context).
 *
 * [GitProperty] enum is generated from [BuildInfoVars] in buildSrc/ResourcesExt.kt.
 */
internal expect fun resolveGitInfo(property: GitProperty): String

/**
 * Returns true if the given version number string contains "-SNAPSHOT"
 */
fun String.isSnapshot(): Boolean {
  return this.contains("-SNAPSHOT")
}
