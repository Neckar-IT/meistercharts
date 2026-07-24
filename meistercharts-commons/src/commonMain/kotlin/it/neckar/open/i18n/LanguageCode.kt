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
package it.neckar.open.i18n

import it.neckar.open.annotations.TsExport
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/**
 * An ISO 639-1 language code, e.g. "de".
 *
 * Distinct from [Locale], which is a language *tag* and carries the region as well ("de-DE").
 * Use [LanguageCode] wherever only the spoken language matters — the region would be noise there.
 *
 * A nullable [LanguageCode] is the idiomatic way to express "language unknown / detect it":
 * `null` means the consumer decides, a value pins the language.
 */
@JvmInline
@Serializable
@TsExport
value class LanguageCode(val value: String) {
  init {
    require(value.length == 2 && value.all { it.isLowerCase() }) {
      "A language code must consist of two lower-case letters (ISO 639-1) but was [$value]"
    }
  }

  override fun toString(): String {
    return value
  }

  companion object {
    val De: LanguageCode = LanguageCode("de")
    val En: LanguageCode = LanguageCode("en")
    val Fr: LanguageCode = LanguageCode("fr")
  }
}

/**
 * The language subtag of this locale, dropping the region:
 * `Locale.Germany.languageCode == LanguageCode.De`
 */
val Locale.languageCode: LanguageCode
  get() = LanguageCode(locale.substringBefore('-'))
