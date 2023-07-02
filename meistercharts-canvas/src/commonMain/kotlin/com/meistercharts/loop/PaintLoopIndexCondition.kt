/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.loop

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
