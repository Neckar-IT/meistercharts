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

import kotlin.random.Random

/**
 * A seeded [Random] implementation with a bit stream that is stable across Kotlin versions and platforms.
 *
 * [Random.invoke] (the seeded factory of the Kotlin standard library) explicitly does *not* guarantee
 * a stable sequence across Kotlin versions. This class implements the well-defined
 * [SplitMix64](https://prng.di.unimi.it/splitmix64.c) algorithm: the raw bit stream ([nextBits]) and
 * [nextDouble] are pinned here and therefore depend only on the seed - never on the Kotlin version or
 * the platform. The derived helpers ([nextInt] with a bound, [nextLong], …) inherit the standard
 * library's derivation-from-[nextBits], which is not itself pinned; in practice it is stable, and the
 * screenshot-comparing E2E tests catch any drift regardless.
 *
 * Use this class whenever generated values must remain reproducible over time - e.g. for demo data
 * that is verified by screenshot-comparing E2E tests.
 */
class StableRandom(seed: Long) : Random() {
  private var state: Long = seed

  override fun nextBits(bitCount: Int): Int {
    require(bitCount in 0..32) { "bitCount must be in 0..32 but was <$bitCount>" }

    if (bitCount == 0) {
      return 0
    }

    state += -0x61c8864680b583ebL // 0x9E3779B97F4A7C15
    var z = state
    z = (z xor (z ushr 30)) * -0x40a7b892e31b1a47L // 0xBF58476D1CE4E5B9
    z = (z xor (z ushr 27)) * -0x6b2fb644ecceee15L // 0x94D049BB133111EB
    z = z xor (z ushr 31)

    return (z ushr (64 - bitCount)).toInt()
  }

  /**
   * Pinned explicitly (same derivation the Kotlin standard library currently uses) so the generated
   * sequence cannot change should the base class implementation ever change.
   */
  override fun nextDouble(): Double {
    val high26 = nextBits(26).toLong()
    val low27 = nextBits(27).toLong()
    return ((high26 shl 27) + low27) / (1L shl 53).toDouble()
  }
}
