package it.neckar.open.collections

import java.util.BitSet

/**
 * Helper class for bit set util related stuff
 */
object BitSetUtils {
  /**
   * Converts a bool array to a bit set
   */
  @JvmStatic
  fun toBitSet(booleans: BooleanArray): BitSet {
    val bitSet = BitSet()
    for (i in booleans.indices) {
      val beamState = booleans[i]
      bitSet[i] = beamState
    }
    return bitSet
  }

  @JvmStatic
  fun toBitSets(booleans: List<BooleanArray>): List<BitSet> {
    return booleans.map {
      toBitSet(it)
    }
  }
}
