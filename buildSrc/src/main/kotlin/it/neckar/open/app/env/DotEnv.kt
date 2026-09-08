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
package it.neckar.open.app.env

import kotlinx.io.files.Path

/**
 * One `.env` file's values, answered with the process environment taking precedence — a variable
 * exported in the shell, or injected by CI, beats the file a developer keeps for everyday runs.
 *
 * [environmentLookup] and [FileSystemAccess] are parameters rather than `expect` declarations, so
 * each target binds them once and a test passes its own. `{ null }` reduces this to the file.
 */
class DotEnv(
  /** What the `.env` file itself carries, without the process environment laid over it. */
  val valuesFromFile: Map<String, String>,
  /** The process environment of the platform this runs on; null for a name it does not carry. */
  private val environmentLookup: (String) -> String?,
) {
  /**
   * [name] from the process environment, falling back to the `.env` file; null when neither carries
   * it.
   *
   * A blank value counts as absent on both sides: `KEY=` in the file is a leftover, not a
   * configured empty string.
   */
  operator fun get(name: String): String? = environmentLookup(name) ?: valuesFromFile[name]?.takeIf { it.isNotBlank() }

  companion object {
    /** An empty [DotEnv] — no file, no values; every lookup falls through to [environmentLookup]. */
    fun empty(environmentLookup: (String) -> String?): DotEnv = DotEnv(emptyMap(), environmentLookup)

    /**
     * Reads [envFileName], searched from [startDirectory] upwards to the git root. A missing file
     * is not an error: the result then carries nothing but the process environment.
     */
    fun load(
      startDirectory: Path,
      envFileName: EnvFileName,
      fileSystem: FileSystemAccess,
      environmentLookup: (String) -> String?,
    ): DotEnv {
      val envFilePath = EnvFileLocator.findEnvFile(startDirectory, envFileName, fileSystem) ?: return empty(environmentLookup)
      return DotEnv(fileSystem.readEnvFile(envFilePath), environmentLookup)
    }
  }
}
