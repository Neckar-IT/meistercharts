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
 * The single upwards search: a service started from a module directory sees the same `.env` as one
 * started from the repository root, and the search stops at the git root — a developer's home
 * directory is not a configuration source.
 */
object EnvFileLocator {
  /**
   * The path of [envFileName], searched from [startDirectory] upwards and stopping at the git root.
   * Null when no such file exists on the way up.
   */
  fun findEnvFile(
    startDirectory: Path,
    envFileName: EnvFileName,
    fileSystem: FileSystemAccess,
  ): Path? {
    var currentDirectory: Path? = fileSystem.absolutePath(startDirectory)

    while (currentDirectory != null && fileSystem.directoryExists(currentDirectory)) {
      val candidate = Path(currentDirectory, envFileName.value)
      if (fileSystem.fileExists(candidate)) {
        return candidate
      }

      if (fileSystem.isGitRoot(currentDirectory)) {
        return null
      }

      currentDirectory = currentDirectory.parent
    }

    return null
  }

  /** A worktree's `.git` is a file pointing at the shared directory, a plain checkout's is a directory. */
  private fun FileSystemAccess.isGitRoot(directory: Path): Boolean {
    val gitMarker = Path(directory, ".git")
    return directoryExists(gitMarker) || fileExists(gitMarker)
  }
}
