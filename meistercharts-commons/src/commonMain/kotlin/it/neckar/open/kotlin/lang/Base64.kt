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

import kotlin.io.encoding.Base64.PaddingOption


/**
 * Converts a byte array to base64
 *
 * Use [toBase64Url] when encoding for URLs
 */
fun ByteArray.toBase64(useUrlEncoding: Boolean = false): String {
  return if (useUrlEncoding) {
    kotlin.io.encoding.Base64.UrlSafe.encode(this)
  } else {
    kotlin.io.encoding.Base64.encode(this)
  }
}

fun String.toBase64(useUrlEncoding: Boolean = false): String {
  return this.encodeToByteArray().toBase64(useUrlEncoding)
}

fun String.fromBase64(useUrlEncoding: Boolean = false): ByteArray {
  return if (useUrlEncoding) {
    kotlin.io.encoding.Base64.UrlSafe.decode(this)
  } else {
    kotlin.io.encoding.Base64.decode(this)
  }
}

fun String.fromBase64String(): String {
  return fromBase64().decodeToString()
}

/**
 * Converts a base64 encoded string to byte array - uses only characters that are allowed in an URL
 */
fun ByteArray.toBase64Url(): String {
  return kotlin.io.encoding.Base64.UrlSafe.encode(this)
}

/**
 * Converts a base64 encoded string to byte array - uses only characters that are allowed in an URL
 */
fun ByteArray.toBase64UrlString(): String {
  return this.toBase64Url()
}

fun String.fromBase64Url(): ByteArray {
  return kotlin.io.encoding.Base64.UrlSafe.decode(addBase64Padding(this))
}

//Workaround for https://youtrack.jetbrains.com/issue/KT-69846
private fun addBase64Padding(encoded: String): String {
  val paddingSize = (4 - (encoded.length % 4)) % 4
  return encoded.padEnd(encoded.length + paddingSize, '=')
}

fun String.fromBase64UrlString(): String {
  return fromBase64Url().decodeToString()
}
