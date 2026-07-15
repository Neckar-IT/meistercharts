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
 * Acknowledges a deliberate allocation inside a `@Hot` body, at the call site.
 *
 * The hot-path Detekt rules (`HotFunctionMustOnlyCallHotFunctions`,
 * `HotFunctionMustNotBoxValueClass`) flag calls to `@Allocates`/`@Slow` code and value-class
 * boxing inside `@Hot` methods. When the allocation is a conscious decision (e.g. once per frame
 * instead of per data point, a rare branch, a cache-miss path), annotate the expression or local
 * variable with `@HotAllocation("reason")` — the finding is suppressed and the decision stays
 * visible and greppable in the code:
 *
 * ```
 * @Hot
 * override fun paint(paintingContext: LayerPaintingContext) {
 *   @HotAllocation("bounding box computed once per frame")
 *   val bounds = chartCalculator.contentAreaRelative2window(0.0, 0.0)
 * }
 * ```
 *
 * The reason is mandatory: it is the typed, project-conformant alternative to the forbidden
 * `@Suppress`. The density of `@HotAllocation` markers in a method is itself a review signal —
 * acknowledging a [AllocationCost.Linear] allocation needs a much better reason than a
 * [AllocationCost.Constant] one.
 *
 * SOURCE retention: the acknowledgement only needs to be visible to the Detekt rule analyzing the
 * same file (and to readers) — it never crosses a module boundary. SOURCE is also required for
 * the `EXPRESSION` target.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.EXPRESSION,
  AnnotationTarget.LOCAL_VARIABLE,
)
annotation class HotAllocation(
  /** Why this allocation is acceptable in the hot path (e.g. `"once per frame"`). */
  val value: String,
)
