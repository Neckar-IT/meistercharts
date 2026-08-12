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

import kotlin.annotation.AnnotationTarget.CLASS

/**
 * Marks a class as a messaging wire type: the content of a WebSocket frame, an SSE message, a
 * webhook body, a queue entry.
 *
 * Messaging crosses the same boundary REST crosses, only over a different transport — so the
 * layer-separation rules of the `neckar-rules` ruleset treat it the same way: a messaging type may
 * carry `@Serializable` and reference other wire types, and Domain classes must not reference it.
 *
 * On a sealed hierarchy the annotation belongs on the root; the leaves inherit it instead of
 * repeating it.
 *
 * The layer is stated here rather than read off the class name (ADL 0179). A name-based exemption
 * fails in the wrong direction: a domain concept that happens to end in a transport word leaves the
 * rules silently, while a forgotten annotation only produces a finding. The alternative is a
 * `messaging` package segment — the same deliberate act, made once for a whole protocol, and the
 * way for modules that do not want this dependency.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(CLASS)
annotation class Messaging
