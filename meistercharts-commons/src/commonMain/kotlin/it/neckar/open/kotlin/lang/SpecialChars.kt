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
package it.neckar.open.kotlin.lang

object SpecialChars {
  /**
   * Non-breaking space
   */
  const val nbsp: Char = Typography.nbsp

  /**
   * Narrow non-breaking space
   */
  const val nnbsp: Char = '\u202F'

  const val thinSpace: Char = '\u2009'
  const val hairSpace: Char = '\u200A'
  const val zeroWidthSpace: Char = '\u200B'
  const val enSpace: Char = '\u2002'
  const val emSpace: Char = '\u2003'
  const val figureSpace: Char = '\u2007'
  const val punctuationSpace: Char = '\u2008'
  const val mathematicalSpace: Char = '\u205F'
}

/**
 * Replaces all uncommon spaces with default spaces.
 * This method can be used to sanitize strings before they are printed to the console
 */
fun String.replaceUnusualSpaces(): String {
  val stringBuilder = StringBuilder(this.length)

  for (char in this) {
    when (char) {
      SpecialChars.nbsp,
      SpecialChars.nnbsp, // Narrow No-Break Space (NNBSP)
      SpecialChars.thinSpace, // Thin Space
      SpecialChars.hairSpace, // Hair Space
      SpecialChars.zeroWidthSpace, // Zero Width Space
      SpecialChars.enSpace, // En Space
      SpecialChars.emSpace, // Em Space
      SpecialChars.figureSpace, // Figure Space
      SpecialChars.punctuationSpace, // Punctuation Space
      SpecialChars.mathematicalSpace,  // Medium Mathematical Space (MMSP)
      -> stringBuilder.append(' ')

      else -> stringBuilder.append(char)
    }
  }

  return stringBuilder.toString()
}
