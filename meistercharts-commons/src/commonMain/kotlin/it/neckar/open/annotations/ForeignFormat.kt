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
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

/**
 * Marks a decoder that drops unknown keys (`strictMode = false` in kaml, `ignoreUnknownKeys = true` in
 * kotlinx.serialization JSON) because a sender outside this repository defines the format.
 *
 * [sender] names that sender, e.g. `GitLab webhooks` or `pnpm, which owns pnpm-workspace.yaml`. A format this
 * repository defines parses strictly, so a misspelt key fails with file and key. The Detekt rule
 * `UnknownKeysOnlyFromForeignSender` reports every lenient decoder that carries neither this annotation on itself or
 * an enclosing declaration nor an entry in the rule's allowlist.
 */
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@Target(CLASS, FUNCTION, PROPERTY)
annotation class ForeignFormat(val sender: String)
