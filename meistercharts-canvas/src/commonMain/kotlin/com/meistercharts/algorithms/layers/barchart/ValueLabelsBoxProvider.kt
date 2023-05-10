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

import com.meistercharts.algorithms.ChartCalculator
import com.meistercharts.provider.BoxProvider1

/**
 * Provides the content area
 */
object ContentAreaBoxProvider : BoxProvider1<ChartCalculator> {
  override fun getX(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2windowX(0.0)
  }

  override fun getY(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2windowY(0.0)
  }

  override fun getWidth(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2zoomedX(1.0)
  }

  override fun getHeight(param0: ChartCalculator): Double {
    return param0.contentAreaRelative2zoomedY(1.0)
  }
}

/**
 * Provides the window bounds
 */
object WindowBoxProvider : BoxProvider1<ChartCalculator> {
  override fun getX(param0: ChartCalculator): Double {
    return 0.0
  }

  override fun getY(param0: ChartCalculator): Double {
    return 0.0
  }

  override fun getWidth(param0: ChartCalculator): Double {
    return param0.chartState.windowWidth
  }

  override fun getHeight(param0: ChartCalculator): Double {
    return param0.chartState.windowHeight
  }
}
