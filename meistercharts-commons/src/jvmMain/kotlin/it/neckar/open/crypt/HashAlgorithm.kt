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


import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 * Represents an algorithm
 *
 */
data class HashAlgorithm(
  /**
   * The name of the algorithm
   */
  val name: String,

  /**
   * The expected length of the hash value in bits.
   */
  val expectedLength: Int,

  /**
   * The alternative names for the algorithm
   */
  val alternativeNames: List<String>,
) {

  constructor(
    name: String,
    expectedLength: Int,
    vararg alternativeNames: String,
  ) : this(
    name,
    expectedLength,
    alternativeNames.toList()
  )

  init {
    require(expectedLength > 0) { "expectedLength must be > 0" }
  }

  /**
   * Returns true if the algorithm matches the given name.
   */
  fun matchesName(algorithmName: String): Boolean {
    return name == algorithmName || alternativeNames.contains(algorithmName)
  }

  fun calculate(value: ByteArray): Hash {
    return calculate(messageDigest = createMessageDigest(), value = value)
  }

  fun calculate(value: String): Hash {
    return calculate(messageDigest = createMessageDigest(), value = value)
  }

  fun calculate(messageDigest: MessageDigest, value: String): Hash {
    return calculate(messageDigest = messageDigest, value = value.toByteArray(StandardCharsets.UTF_8))
  }

  fun calculate(messageDigest: MessageDigest, value: ByteArray): Hash {
    messageDigest.reset()
    messageDigest.update(value)

    val digest = messageDigest.digest()
    return Hash(algorithm = this, value = digest)
  }

  fun calculate(resource: URL): Hash {
    return calculate(messageDigest = createMessageDigest(), resource = resource)
  }

  fun calculate(messageDigest: MessageDigest, resource: URL): Hash {
    resource.openStream().use {
      return calculate(algorithm = this, messageDigest = messageDigest, resource = it)
    }
  }

  fun calculate(file: File): Hash {
    return calculate(messageDigest = createMessageDigest(), file = file)
  }

  fun calculate(messageDigest: MessageDigest, file: File): Hash {
    file.inputStream().buffered().use {
      return calculate(algorithm = this, messageDigest = messageDigest, resource = it)
    }
  }

  fun calculate(resourceIn: InputStream): Hash {
    return calculate(algorithm = this, messageDigest = createMessageDigest(), resource = resourceIn)
  }

  override fun toString(): String {
    return name
  }

  /**
   * Creates a new message digest for the algorithm
   */
  fun createMessageDigest(): MessageDigest {
    return MessageDigest.getInstance(name)
  }

  companion object {
    val MD5: HashAlgorithm = HashAlgorithm("MD5", 128)

    val SHA1: HashAlgorithm = HashAlgorithm("SHA1", 128, "SHA-1")
    val SHA224: HashAlgorithm = HashAlgorithm("SHA-224", 224, "SHA224")
    val SHA256: HashAlgorithm = HashAlgorithm("SHA-256", 256, "SHA256")
    val SHA384: HashAlgorithm = HashAlgorithm("SHA-384", 384, "SHA384")
    val SHA3_224: HashAlgorithm = HashAlgorithm("SHA3-224", 224)
    val SHA3_256: HashAlgorithm = HashAlgorithm("SHA3-256", 256)
    val SHA3_384: HashAlgorithm = HashAlgorithm("SHA3-384", 384)
    val SHA3_512: HashAlgorithm = HashAlgorithm("SHA3-512", 512)
    val SHA512: HashAlgorithm = HashAlgorithm("SHA-512", 512, "SHA512")

    val entries: List<HashAlgorithm> = listOf(MD5, SHA1, SHA224, SHA256, SHA384, SHA3_224, SHA3_256, SHA3_384, SHA3_512, SHA512)

    fun values(): List<HashAlgorithm> {
      return entries
    }

    /**
     * Finds the algorithm by its name.
     */

    fun findByName(algorithmName: String): HashAlgorithm? {
      return entries.firstOrNull { it.matchesName(algorithmName) }
    }

    fun getByName(algorithmName: String): HashAlgorithm {
      return findByName(algorithmName) ?: throw IllegalArgumentException("Unknown algorithm: $algorithmName")
    }

    /**
     * Calculates the hash for the given resource using the given algorithm and message digest.
     */
    fun calculate(algorithm: HashAlgorithm, messageDigest: MessageDigest, resource: InputStream): Hash {
      require(messageDigest.algorithm == algorithm.name) { "Algorithm name must not be the same. Was $algorithm but message digest has ${messageDigest.algorithm}" }

      messageDigest.reset()

      val cache = ByteArray(255)
      while (true) {
        val read = resource.read(cache, 0, 255)
        if (read <= 0) {
          break
        }
        messageDigest.update(cache, 0, read)
      }

      val digest = messageDigest.digest()
      return Hash(algorithm = algorithm, value = digest)
    }
  }
}
