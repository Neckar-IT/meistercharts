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
package it.neckar.logging

/**
 * Returns a [Logger] named after the receiver's runtime class (its fully qualified name).
 *
 * Inlined and reified: zero reflection, zero allocation beyond the SLF4J call itself.
 *
 * ```kotlin
 * class FooService {
 *   private val logger = logger()
 * }
 * ```
 *
 * For `companion object` properties prefer the symmetric form [LoggerFactory.getLogger]`<MyClass>()`
 * — declaring `logger()` inside a companion resolves [T] to `Companion`, not the enclosing class.
 *
 * JVM-only by design: SLF4J semantics are JVM-anchored, and Kotlin/JS would silently degrade the
 * logger name to the simple name (no `KClass.qualifiedName` on JS for general classes).
 */
inline fun <reified T : Any> T.logger(): Logger = LoggerFactory.getLogger(T::class)

/**
 * Returns a [Logger] named after the type argument [T]. Intended for `companion object` properties
 * where `this` is the companion and [logger] would resolve to `Companion`.
 *
 * ```kotlin
 * class FooService {
 *   companion object {
 *     private val logger = LoggerFactory.getLogger<FooService>()
 *   }
 * }
 * ```
 *
 * JVM-only by design (see [logger]).
 */
inline fun <reified T : Any> LoggerFactory.getLogger(): Logger = getLogger(T::class)
