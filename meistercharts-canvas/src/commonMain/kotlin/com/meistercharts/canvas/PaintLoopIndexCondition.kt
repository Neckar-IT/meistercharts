package com.meistercharts.canvas

/**
 * Represents a condition that must be fulfilled during a paint loop index
 */
interface PaintLoopIndexCondition {
  companion object {
    fun <T> isEqual(initialValue: T): IsSameInPaintLoop<T> {
      return IsSameInPaintLoop(initialValue)
    }

    fun isEqualInt(): IsSameIntInPaintLoop {
      return IsSameIntInPaintLoop()
    }
  }
}

/**
 * Verifies that a value is the same within the same paint loop
 */
class IsSameInPaintLoop<T>(initialValue: T) : PaintLoopIndexCondition {
  private var lastLoopIndex = PaintingLoopIndex.Unknown
  private var lastValue: T = initialValue

  fun verifySame(loopIndex: PaintingLoopIndex, valueToCompare: T, messageProvider: () -> String) {
    //Verify that the data series index has not changed in the same loop
    if (lastLoopIndex == loopIndex) {
      require(lastValue == valueToCompare, messageProvider)
    } else {
      lastLoopIndex = loopIndex
      lastValue = valueToCompare
    }
  }
}

class IsSameIntInPaintLoop : PaintLoopIndexCondition {
  private var lastLoopIndex = PaintingLoopIndex.Unknown
  private var lastValue: Int = Int.MIN_VALUE

  fun verifySame(loopIndex: PaintingLoopIndex, valueToCompare: Int, messageProvider: () -> String) {
    //Verify that the data series index has not changed in the same loop
    if (lastLoopIndex == loopIndex) {
      require(lastValue == valueToCompare, messageProvider)
    } else {
      lastLoopIndex = loopIndex
      lastValue = valueToCompare
    }
  }
}
