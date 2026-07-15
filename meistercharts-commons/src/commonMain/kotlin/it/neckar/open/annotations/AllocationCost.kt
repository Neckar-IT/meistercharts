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
 * Structural cost class of an allocation marked with [Allocates].
 *
 * Deliberately an objective classification (decidable by reading the code), not a subjective
 * severity: the two values differ in *how the allocation scales*, which is what matters for
 * hot-path prioritization. Both classes are denied in `@Hot` bodies and can be acknowledged with
 * [HotAllocation]; a [Linear] acknowledgement needs a much better reason than a [Constant] one.
 */
enum class AllocationCost {
  /**
   * A fixed number of small objects per call (typically 1-3), independent of input size — e.g.
   * one `Coordinates` per conversion, a `copy()` of a data class, a SAM instance per read.
   */
  Constant,

  /**
   * Allocation scales with input size — e.g. `copyOfRange`, `map`/`filter`/`toList`,
   * `reversed()`, `joinToString`, builders producing collections.
   */
  Linear,
}
