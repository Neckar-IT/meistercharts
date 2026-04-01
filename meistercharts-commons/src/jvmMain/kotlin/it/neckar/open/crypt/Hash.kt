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
  val algorithm: HashAlgorithm,
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
    fun fromHex(algorithm: HashAlgorithm, valueAsHex: String): Hash {
      try {
        return Hash(algorithm, Hex.decodeHex(valueAsHex.toCharArray()))
      } catch (e: DecoderException) {
        throw IllegalArgumentException("Invalid hex string <$valueAsHex>", e)
      }
    }
  }
}
