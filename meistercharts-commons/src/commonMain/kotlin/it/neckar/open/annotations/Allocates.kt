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
 * Annotates methods that allocate objects - and should not be called too often to avoid unnecessary garbage collection
 *
 * Also the marker for properties whose accessor allocates: property accessors are assumed
 * allocation-free by default; `@Allocates` on a property (or getter) declares the exception.
 *
 * Acts as an explicit negative marker for the hot path: `HotFunctionMustOnlyCallHotFunctions`
 * flags any call to an `@Allocates` function (and any read of an `@Allocates` property)
 * from a `@Hot` body — regardless of `inline` or whitelist exemptions. `inline` does not mean
 * allocation-free; this marker closes exactly that hole. A deliberate allocation is acknowledged
 * at the call site with [HotAllocation].
 *
 * The mandatory [AllocationCost] classifies how the allocation scales — [AllocationCost.Constant]
 * (fixed number of small objects) vs. [AllocationCost.Linear] (scales with input size) — and
 * drives hot-path prioritization.
 *
 * The marker applies platform-independently — including small single-object returns (e.g. a
 * `Coordinates` from a conversion): only the JVM's escape analysis reliably elides those. On JS
 * (V8: top-tier-only, small inlining budget) elision is unreliable, on Kotlin/Wasm (WasmGC)
 * practically absent — and JS today / Wasm prospectively are the primary render targets. Hot-path
 * callers use the allocation-free alternatives instead (axis-separated `Double` overloads like
 * `window2domainX`/`window2domainY`, inline-callback variants, scratch objects).
 *
 * BINARY retention so the marker stays visible to static analysis when the callee comes from an
 * already-compiled module.
 */
@Retention(AnnotationRetention.BINARY)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.TYPE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.LOCAL_VARIABLE,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.TYPE,
  AnnotationTarget.FILE,
  AnnotationTarget.TYPEALIAS
)
@MustBeDocumented
annotation class Allocates(
  /** How the allocation scales per call — the basis for hot-path prioritization. */
  val value: AllocationCost,
)
