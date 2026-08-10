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
 * Marks an enum whose entries carry additional properties, so those properties survive into the
 * generated OpenAPI schema and from there into TypeScript.
 *
 * Without this annotation the OpenAPI generator emits a plain string enum (`EnumSchema`) and the
 * entry properties are lost. With it, the generator emits a `ComponentEnumSchemaObject` instead,
 * one object per entry - see `SchemaDescriptor` in `internal/open/rest/openapi-generator/`.
 * `it.neckar.rest.PlatformHeader` is the reference usage: its `headerName` literals reach the
 * frontend only because of this annotation.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME) //The OpenAPI generator looks this annotation up reflectively at runtime (findAnnotation) - BINARY would not be visible there
annotation class ExportAsObject() {

  companion object {
    /**
     * The simple name of this annotation class, for consumers that match annotations by short name
     * instead of by class reference (as a KSP processor does, which cannot resolve a `KClass`).
     *
     * Nothing in this repository reads it at the moment - the OpenAPI generator matches by class.
     */
    const val AnnotationName: String = "ExportAsObject"

    @Deprecated("Typo. Use AnnotationName instead.", ReplaceWith("AnnotationName"))
    const val AnnotationMame: String = AnnotationName
  }
}
