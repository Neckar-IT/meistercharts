/**
 * Copyright (C) cedarsoft GmbH.
 *
 * Licensed under the GNU General Public License version 3 (the "License")
 * with Classpath Exception; you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.cedarsoft.org/gpl3ce
 * (GPL 3 with Classpath Exception)
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 3 only, as
 * published by the Free Software Foundation. cedarsoft GmbH designates this
 * particular file as subject to the "Classpath" exception as provided
 * by cedarsoft GmbH in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 3 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 3 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
 * or visit www.cedarsoft.com if you need additional information or
 * have any questions.
 */

package it.neckar.open.crypt

import org.apache.commons.codec.DecoderException
import org.apache.commons.codec.binary.Hex
import java.io.Serializable

/**
 * Represents a hash value
 *
 */
class Hash(
  /**
   * The algorithm used to calculate the hash value
   */
  val algorithm: Algorithm,
  /**
   * The hash value
   */
  value: ByteArray,
) : Serializable {

  private val value: ByteArray = value.clone()

  /**
   * Returns the hash value as hex string
   */
  val valueAsHex: String
    get() = String(Hex.encodeHex(value))

  fun getValue(): ByteArray {
    return value.clone()
  }

  override fun toString(): String {
    return "[$algorithm: $valueAsHex]"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Hash) return false

    if (algorithm != other.algorithm) return false
    if (!value.contentEquals(other.value)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = algorithm.hashCode()
    result = 31 * result + value.contentHashCode()
    return result
  }

  companion object {
    private const val serialVersionUID = 5728176239480983210L

    /**
     * Creates a hash from the given hex value
     *
     * @param algorithm  the algorithm
     * @param valueAsHex the hex value
     * @return the hash
     */
    @JvmStatic
    fun fromHex(algorithm: Algorithm, valueAsHex: String): Hash {
      try {
        return Hash(algorithm, Hex.decodeHex(valueAsHex.toCharArray()))
      } catch (e: DecoderException) {
        throw IllegalArgumentException("Invalid hex string <$valueAsHex>", e)
      }
    }
  }
}
