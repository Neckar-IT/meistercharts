/*
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
package com.meistercharts.demo

import com.meistercharts.algorithms.tooltip.balloon.AbsoluteNosePositionCalculator
import com.meistercharts.algorithms.tooltip.balloon.BalloonTooltipPaintable
import it.neckar.geometry.Direction


/**
 * Adds methods to configure the nose positions
 */
@DemoDeclaration
fun MeisterchartsDemo.configurableNosePosition(tooltipPainter: BalloonTooltipPaintable) {
  configurableDouble("Relative Nose Position", 0.5, false) {
    onChange {
      tooltipPainter.style.relativeNosePosition(it)
      markAsDirty()
    }
  }

  configurableDouble("Absolute Nose Position", 10.0, false) {
    max = 100.0

    onChange {
      val old = tooltipPainter.style.nosePositionCalculator
      val oldValue = (old as? AbsoluteNosePositionCalculator)?.direction ?: Direction.TopLeft

      tooltipPainter.style.absoluteNosePosition(it, oldValue)
      markAsDirty()
    }
  }

  configurableEnum("Absolute Nose Pos Dir", Direction.TopLeft) {
    onChange {
      val old = tooltipPainter.style.nosePositionCalculator
      val oldValue = (old as? AbsoluteNosePositionCalculator)?.absoluteNosePosition ?: 10.0

      tooltipPainter.style.absoluteNosePosition(oldValue, it)
      markAsDirty()
    }
  }

}
