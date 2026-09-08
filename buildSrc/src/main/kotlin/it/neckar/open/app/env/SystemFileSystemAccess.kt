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

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString

/**
 * The real file system, through kotlinx-io: one implementation that answers the same on the JVM, on
 * Kotlin/Native and in a build script.
 */
object SystemFileSystemAccess : FileSystemAccess {
  override fun fileExists(path: Path): Boolean = SystemFileSystem.metadataOrNull(path)?.isRegularFile == true

  override fun directoryExists(path: Path): Boolean = SystemFileSystem.metadataOrNull(path)?.isDirectory == true

  /** Null for a directory as well: opening one as a source fails rather than yielding an empty file. */
  override fun readFileOrNull(path: Path): String? = runCatching { SystemFileSystem.source(path).buffered().use { it.readString() } }.getOrNull()

  /** An unresolvable path stays as it came in — the upwards search asks about directories that may not exist. */
  override fun absolutePath(path: Path): Path = runCatching { SystemFileSystem.resolve(path) }.getOrElse { path }
}
