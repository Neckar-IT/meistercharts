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
 * Acknowledges a **polymorphic delegation point** inside a `@Hot` body: a call that dispatches
 * over an interface which is deliberately not `@Hot`-colored (yet), because coloring it would
 * cascade onto hundreds of overrides (e.g. `Layer.paint` with ~284 implementations).
 *
 * `HotFunctionMustOnlyCallHotFunctions` suppresses its "not itself `@Hot`" finding for the
 * annotated expression/statement. The `@Hot` guarantee of the caller then explicitly ends at this
 * boundary — the wrapper itself is allocation-checked, the delegate is checked wherever it is
 * colored itself:
 *
 * ```
 * @Hot
 * override fun paint(paintingContext: LayerPaintingContext) {
 *   @HotBoundary("polymorphic layer delegation - delegate is colored where implemented")
 *   delegate.paint(paintingContext)
 * }
 * ```
 *
 * Deliberately distinct from [HotAllocation]: that acknowledges a known **allocation**
 * (`@Slow`/`@Allocates` deny), this acknowledges an **un-colorable dispatch edge**. The reason is
 * mandatory. Every `@HotBoundary` is a greppable TODO: once the target interface gets colored at
 * the end of the rollout, the marker is removed and the chain closes end to end.
 *
 * SOURCE retention: only the Detekt rule analyzing the same file (and readers) need to see it.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.EXPRESSION,
  AnnotationTarget.LOCAL_VARIABLE,
)
annotation class HotBoundary(
  /** Why this dispatch edge cannot be colored yet (e.g. `"polymorphic layer delegation"`). */
  val value: String,
)
