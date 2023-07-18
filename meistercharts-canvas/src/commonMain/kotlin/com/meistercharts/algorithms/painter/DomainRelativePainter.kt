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

import com.meistercharts.calc.ChartCalculator

/**
 * A DomainRelativePainter contains a [ChartCalculator].
 * Implementations are able to paint data that is not converted to the [com.meistercharts.algorithms.Window]
 * system but instead provided as [com.meistercharts.algorithms.Domain] values.
 *
 */
@Deprecated("Do these conversions in a layer")
interface DomainRelativePainter : Painter {
  /**
   * Returns the chart calculator
   */
  val calculator: ChartCalculator
}
