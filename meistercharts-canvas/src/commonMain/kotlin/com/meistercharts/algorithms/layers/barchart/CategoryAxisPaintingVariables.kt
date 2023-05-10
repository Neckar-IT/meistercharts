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
package com.meistercharts.algorithms.layers.barchart

import com.meistercharts.algorithms.layers.AxisPaintingVariables
import com.meistercharts.algorithms.layers.AxisPaintingVariablesImpl
import com.meistercharts.algorithms.layout.EquisizedBoxLayout

interface CategoryAxisPaintingVariables : AxisPaintingVariables {
  /**
   * The layout that is used
   */
  var categoryLayout: EquisizedBoxLayout
}


abstract class CategoryAxisPaintingVariablesImpl : CategoryAxisPaintingVariables, AxisPaintingVariablesImpl() {

  /**
   * The layout
   */
  override var categoryLayout: EquisizedBoxLayout = EquisizedBoxLayout.empty

  /**
   * Resets all variables to their default values
   */
  override fun reset() {
    super.reset()

    categoryLayout = EquisizedBoxLayout.empty
  }

}
