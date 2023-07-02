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
package com.meistercharts.algorithms.layers

import com.meistercharts.loop.PaintingLoopIndex

/**
 * Tagging interface for calculated layout variables.
 *
 * Layer properties have been calculated in the [Layer.layout] method.
 * They are used in the [Layer.paint] method. They can also be used to look
 * up positions/locations or for other layers to reuse existing calculations.
 *
 * Attention: There also exist painting properties for the chart that are managed by [PaintingProperties].
 */
interface PaintingVariables {
  /**
   * Will be called in layout
   */
  fun calculate(paintingContext: LayerPaintingContext)
}

/**
 * No properties for this layer
 */
object None : PaintingVariables {
  override fun calculate(paintingContext: LayerPaintingContext) {
    //Do nothing
  }
}

/**
 * Interface for classes that have a loop index
 */
interface LoopIndexAware {
  /**
   * The current loop index
   */
  val loopIndex: PaintingLoopIndex

  fun verifyLoopIndex(paintingContext: LayerPaintingContext) {
    verifyLoopIndex(paintingContext.loopIndex)
  }

  /**
   * Verifies that calculate has been called for the current loop index
   */
  fun verifyLoopIndex(expectedLoopIndex: PaintingLoopIndex) {
    check(loopIndex == expectedLoopIndex) {
      "Invalid loop index ($loopIndex) but expected $expectedLoopIndex. Call [calculate] in every paint loop"
    }
  }
}

/**
 * Painting variables that store the current [PaintingLoopIndex].
 * Offer some verify methods that ensure the calculate methods have been called.
 *
 * Use [AbstractPaintingVariables] instead!
 */
interface LoopIndexAwarePaintingVariables : LoopIndexAware, PaintingVariables {
}

/**
 * Abstract base class that automatically saves the loop index.
 * And offers methods to verify that [calculate] has been called.
 *
 * Subclasses must overwrite [calculate] - and call `super.calculate(paintingContext)`
 */
abstract class AbstractPaintingVariables : LoopIndexAwarePaintingVariables {
  override var loopIndex: PaintingLoopIndex = PaintingLoopIndex.Unknown

  open fun reset() {
    this.loopIndex = PaintingLoopIndex.Unknown
  }

  override fun calculate(paintingContext: LayerPaintingContext) {
    this.loopIndex = paintingContext.loopIndex
  }
}
