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
package it.neckar.open.annotations

/**
 * Marks a file as written by a generator: an edit to it survives until the next generator run and
 * then disappears, without a message.
 *
 * Applied to the file (`@file:Generated(…)`), because what is generated is the whole file — a
 * generator that writes half a file and leaves the other half to a human writes a merge conflict.
 * A generated declaration inside a hand-written file is not this annotation's case; the tools of
 * this repository do not produce one.
 *
 * [by] names the tool, [from] the input it derived the file from. "Do not edit" is half an
 * instruction; the other half is where the edit belongs instead, and only [from] can say it. Both
 * are opaque strings — a repository-relative path reads the same in an editor, in a `git grep` and
 * in a review comment, which is what the generators write.
 *
 *     @file:Generated(
 *       by = "spec-generator/generate_backend.py",
 *       from = "internal/patterns/specs/entities/library/BookReview.yaml",
 *     )
 *
 * [Retention][AnnotationRetention] is RUNTIME so a test can ask a loaded class where it came from,
 * the same way [FormerName] can be asked.
 */
@Target(AnnotationTarget.FILE)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Generated(
  /** The tool that wrote the file, e.g. `spec-generator/generate_backend.py`. */
  val by: String,

  /**
   * The input the file was derived from, repository-relative — e.g. an entity spec.
   *
   * Empty when the file has no single input: a navigation file is derived from every entity of the
   * project at once, and naming one of them would send its reader to the wrong place.
   */
  val from: String = "",
)
