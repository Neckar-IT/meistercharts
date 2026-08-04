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
 * The file system questions the `.env` lookup asks, as a parameter to [EnvFileLocator] and [DotEnv]
 * — so the search stays free of platform API and a test can answer them from a map.
 *
 * Path arithmetic is not among them: [Path.parent] and `Path(directory, name)` need no file system.
 */
interface FileSystemAccess {
  /** True when [path] points at a regular file. */
  fun fileExists(path: Path): Boolean

  /** True when [path] points at a directory. */
  fun directoryExists(path: Path): Boolean

  /** The file's content; null when it does not exist or cannot be read. */
  fun readFileOrNull(path: Path): String?

  /** [path] resolved against the working directory; [path] itself when it does not resolve. */
  fun absolutePath(path: Path): Path
}

/** The values [envFile] carries; empty when it does not exist or cannot be read. */
fun FileSystemAccess.readEnvFile(envFile: Path): Map<String, String> = readFileOrNull(envFile)?.let(EnvFileParser::parse).orEmpty()
