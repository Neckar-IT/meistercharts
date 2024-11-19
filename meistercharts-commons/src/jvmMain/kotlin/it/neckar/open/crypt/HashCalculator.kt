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
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

/**
 *
 * HashCalculator class.
 *
 */
object HashCalculator {

  @JvmStatic
  fun calculate(algorithm: Algorithm, value: ByteArray): Hash {
    return calculate(algorithm.messageDigest, value)
  }

  @JvmStatic
  fun calculate(algorithm: Algorithm, value: String): Hash {
    return calculate(algorithm.messageDigest, value)
  }

  @JvmStatic
  fun calculate(messageDigest: MessageDigest, value: String): Hash {
    return calculate(messageDigest, value.toByteArray(StandardCharsets.UTF_8))
  }

  @JvmStatic
  fun calculate(messageDigest: MessageDigest, value: ByteArray): Hash {
    messageDigest.reset()
    messageDigest.update(value)

    val digest = messageDigest.digest()
    return Hash(Algorithm.getAlgorithm(messageDigest.algorithm), digest)
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(algorithm: Algorithm, resource: URL): Hash {
    return calculate(algorithm.messageDigest, resource)
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(messageDigest: MessageDigest, resource: URL): Hash {
    val inputStream = resource.openStream()
    try {
      return calculate(messageDigest, inputStream)
    } finally {
      inputStream.close()
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(algorithm: Algorithm, file: File): Hash {
    return calculate(algorithm.messageDigest, file)
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(messageDigest: MessageDigest, file: File): Hash {
    file.inputStream().buffered().use {
      return calculate(messageDigest, it)
    }
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(algorithm: Algorithm, resourceIn: InputStream): Hash {
    return calculate(algorithm.messageDigest, resourceIn)
  }

  @JvmStatic
  @Throws(IOException::class)
  fun calculate(messageDigest: MessageDigest, resourceIn: InputStream): Hash {
    messageDigest.reset()

    val cache = ByteArray(255)
    while (true) {
      val read = resourceIn.read(cache, 0, 255)
      if (read <= 0) {
        break
      }
      messageDigest.update(cache, 0, read)
    }

    val digest = messageDigest.digest()
    return Hash(Algorithm.getAlgorithm(messageDigest.algorithm), digest)
  }
}
