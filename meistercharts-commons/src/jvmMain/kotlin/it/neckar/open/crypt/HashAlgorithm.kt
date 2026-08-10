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
  val expectedLengthInBits: Int,

  /**
   * The alternative names for the algorithm
   */
  val alternativeNames: List<String>,
) {

  constructor(
    name: String,
    expectedLengthInBits: Int,
    vararg alternativeNames: String,
  ) : this(
    name,
    expectedLengthInBits,
    alternativeNames.toList()
  )

  init {
    require(expectedLengthInBits > 0) { "expectedLengthInBits must be > 0" }
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
      return calculate(algorithm = this, messageDigest = messageDigest, inputStream = it)
    }
  }

  fun calculate(file: File): Hash {
    return calculate(messageDigest = createMessageDigest(), file = file)
  }

  fun calculate(messageDigest: MessageDigest, file: File): Hash {
    file.inputStream().buffered().use {
      return calculate(algorithm = this, messageDigest = messageDigest, inputStream = it)
    }
  }

  fun calculate(inputStream: InputStream): Hash {
    return calculate(algorithm = this, messageDigest = createMessageDigest(), inputStream = inputStream)
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
    /**
     * MD5 is cryptographically broken and **must not** be used for authentication, MAC,
     * password hashing, or signature verification. Acceptable only for non-security
     * data-integrity hashing (file fingerprints, cache keys). Use [SHA256] or stronger.
     */
    val MD5: HashAlgorithm = HashAlgorithm("MD5", 128)

    /**
     * SHA-1 is cryptographically broken and **must not** be used for authentication, MAC,
     * password hashing, or signature verification. Acceptable only for non-security
     * data-integrity hashing (file fingerprints, cache keys). Use [SHA256] or stronger.
     */
    val SHA1: HashAlgorithm = HashAlgorithm("SHA1", 160, "SHA-1")
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
     * Calculates the hash for the content of the given input stream using the given algorithm and message digest.
     */
    fun calculate(algorithm: HashAlgorithm, messageDigest: MessageDigest, inputStream: InputStream): Hash {
      require(algorithm.matchesName(messageDigest.algorithm)) { "Algorithm name must match. Was $algorithm but message digest has ${messageDigest.algorithm}" }

      messageDigest.reset()

      val buffer = ByteArray(255)
      while (true) {
        val read = inputStream.read(buffer, 0, buffer.size)
        if (read <= 0) {
          break
        }
        messageDigest.update(buffer, 0, read)
      }

      val digest = messageDigest.digest()
      return Hash(algorithm = algorithm, value = digest)
    }
  }
}
