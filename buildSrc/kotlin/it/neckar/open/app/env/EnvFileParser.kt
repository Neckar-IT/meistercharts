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

/**
 * Reads the `KEY=value` lines of an `.env` file: blank lines, `#` comment lines and lines without
 * `=` are skipped, one layer of matching quotes is stripped, everything after the first `=` is the
 * value.
 *
 * These files are flat credential lists, so interpolation (`PATH=$HOME/bin`), an `export` prefix,
 * multi-line values and trailing comments are not supported — `TOKEN=abc # rotated` yields
 * `abc # rotated`.
 */
object EnvFileParser {
  /** The file's [content] as a map of name to value. */
  fun parse(content: String): Map<String, String> =
    content
      .lineSequence()
      .map { it.trim() }
      .filter { it.isNotEmpty() && it.startsWith("#").not() && it.contains('=') }
      .associate { line ->
        val name = line.substringBefore('=').trim()
        val value = line.substringAfter('=').trim().removeSurroundingQuotes()
        name to value
      }

  /** Strips one layer of matching single or double quotes. */
  private fun String.removeSurroundingQuotes(): String = when {
    length >= 2 && startsWith('"') && endsWith('"') -> substring(1, length - 1)
    length >= 2 && startsWith('\'') && endsWith('\'') -> substring(1, length - 1)
    else -> this
  }
}
