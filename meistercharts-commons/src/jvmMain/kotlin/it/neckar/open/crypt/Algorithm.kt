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


import java.security.MessageDigest

/**
 * Represents an algorithm
 *
 */
class Algorithm
/**
 * Creates a new algorithm.
 * The first alternative name will be used to get the MessageDigest
 */
constructor(val expectedLength: Int, val name: String, vararg alternativeNames: String) {
  /**
   * The alternative names for the algorithm
   */
  val alternativeNames: List<String> = alternativeNames.toList()

  init {
    require(expectedLength > 0) { "expectedLength must be > 0" }
  }

  fun matchesName(algorithmName: String): Boolean {
    return name == algorithmName || alternativeNames.contains(algorithmName)
  }

  override fun toString(): String {
    return name
  }

  /**
   * Creates a new message digest for the algorithm
   */
  val messageDigest: MessageDigest
    get() {
      return MessageDigest.getInstance(name)
    }

  companion object {
    val MD5: Algorithm = Algorithm(128, "MD5")
    val SHA1: Algorithm = Algorithm(128, "SHA1", "SHA-1")
    val SHA256: Algorithm = Algorithm(256, "SHA-256", "SHA256")
    val SHA512: Algorithm = Algorithm(512, "SHA-512", "SHA512")

    fun values(): List<Algorithm> {
      return listOf(MD5, SHA1, SHA256, SHA512)
    }

    @JvmStatic
    fun getAlgorithm(algorithmName: String): Algorithm {
      //Now search for the alternative names
      for (algorithm in values()) {
        if (algorithm.matchesName(algorithmName)) {
          return algorithm
        }
      }
      throw IllegalArgumentException("No algorithm found for $algorithmName")
    }

    @Deprecated("Use getAlgorithm instead", ReplaceWith("getAlgorithm(algorithmName)"))
    fun valueOf(value: String): Algorithm {
      return when (value) {
        "MD5" -> MD5
        "SHA1" -> SHA1
        "SHA256" -> SHA256
        "SHA512" -> SHA512
        else -> throw IllegalArgumentException("No object it.neckar.open.crypt.Algorithm.$value")
      }
    }
  }
}
