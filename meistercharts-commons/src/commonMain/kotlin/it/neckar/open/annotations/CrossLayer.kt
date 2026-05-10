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
 * Marks a class as intentionally cross-layer.
 *
 * Cross-layer classes are allowed as property types in Domain, Mongo-Entity, and REST/DataView
 * classes without violating the layer-separation rules enforced by the `neckar-rules` Detekt
 * ruleset (`DomainMustNotReferenceEntity`, `EntityMustNotReferenceDomain`,
 * `DataViewMustNotReferenceDomain`, …).
 *
 * Use sparingly. The default is strict layer separation via conversion extension functions
 * (`toModel`, `toEntity`, `toDetails`, `toPreview`, `toTableEntry`, `toDomain`). Reach for
 * `@CrossLayer` only when a value-class / `*Like` interface / enum cannot model the shared
 * concept (e.g. small structured payloads that genuinely live identically in domain and on the
 * wire).
 *
 * The required [reason] surfaces in code review and pattern docs and prevents `@CrossLayer` from
 * becoming a silent default escape hatch.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@Target(CLASS)
annotation class CrossLayer(val reason: String)
