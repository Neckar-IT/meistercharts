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
 * An annotation to mark a value class to be exported to TypeScript
 * As value classes cannot be annotated with @JsExport, this annotation is used to export them to TypeScript.
 * Technically, other classes would also be collected with this annotation, but this is not recommended.
 *
 * Can be used in JVM projects where @JsExport is not available
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY) //Is required to allow KSP plugin to find the annotation when calling `KSAnnotated.annotations`
annotation class TsExport() {

  companion object {
    /**
     * The name of the JsExport annotation.
     * The JsExport annotation is only available in JS projects.
     */
    const val JsExportAnnotationMame: String = "JsExport"

    /**
     * The name of the `JsExport.Ignore` annotation.
     */
    const val JsExportIgnoredAnnotationMame: String = "Ignore"
  }
}
