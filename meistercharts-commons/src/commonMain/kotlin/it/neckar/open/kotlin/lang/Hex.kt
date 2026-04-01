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

object Hex {
  private const val DIGITS = "0123456789ABCDEF"
  val DIGITS_UPPER: String = DIGITS.uppercase()
  val DIGITS_LOWER: String = DIGITS.lowercase()

  fun decodeChar(c: Char): Int = when (c) {
    in '0'..'9' -> c - '0'
    in 'a'..'f' -> c - 'a' + 10
    in 'A'..'F' -> c - 'A' + 10
    else -> -1
  }

  fun encodeCharLower(v: Int): Char = DIGITS_LOWER[v]
  fun encodeCharUpper(v: Int): Char = DIGITS_UPPER[v]

  fun isHexDigit(c: Char): Boolean = decodeChar(c) >= 0

  fun decode(str: String): ByteArray {
    val out = ByteArray((str.length + 1) / 2)
    var opos = 0
    var nibbles = 0
    var value = 0
    for (n in 0 until str.length) {
      val c = str[n]
      val vv = decodeChar(c)
      if (vv >= 0) {
        value = (value shl 4) or vv
        nibbles++
      }
      if (nibbles == 2) {
        out[opos++] = value.toByte()
        nibbles = 0
        value = 0
      }
    }
    return if (opos != out.size) out.copyOf(opos) else out
  }

  fun encodeLower(src: ByteArray): String = encodeBase(src, DIGITS_LOWER)
  fun encodeUpper(src: ByteArray): String = encodeBase(src, DIGITS_UPPER)

  private fun encodeBase(data: ByteArray, digits: String = DIGITS): String {
    val out = StringBuilder(data.size * 2)
    for (n in 0 until data.size) {
      val v = data[n].toInt() and 0xFF
      out.append(digits[(v ushr 4) and 0xF])
      out.append(digits[(v ushr 0) and 0xF])
    }
    return out.toString()
  }
}

val List<String>.unhexIgnoreSpaces get() = joinToString("").unhexIgnoreSpaces
val String.unhexIgnoreSpaces get() = this.replace(" ", "").replace("\n", "").replace("\r", "").unhex
val String.unhex get() = Hex.decode(this)
val ByteArray.hex get() = Hex.encodeLower(this)

val Int.hex: String get() = "0x$shex"
val Int.shex: String
  get() {
    var out = ""
    for (n in 0 until 8) {
      val v = (this ushr ((7 - n) * 4)) and 0xF
      out += Hex.encodeCharUpper(v)
    }
    return out
  }
