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
package com.meistercharts.algorithms.painter

import com.meistercharts.color.Color
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import it.neckar.open.unit.other.px


/**
 * Abstract base class for xy line painters
 *
 */
abstract class AbstractXyLinePainter
protected constructor(
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), XYPainter {
  /**
   * The stroke color for the line
   */
  var stroke: ColorProvider = Color.black

  /**
   * The shadow color
   */
  var shadow: ColorProviderNullable = { null }

  @px
  var lineWidth: Double = 1.0
}
