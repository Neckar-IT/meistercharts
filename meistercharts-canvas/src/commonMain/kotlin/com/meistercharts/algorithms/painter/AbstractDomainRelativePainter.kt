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

import com.meistercharts.algorithms.ChartCalculator

/**
 * Abstract base class for painters. Implementing painter work with domain relative values
 *
 */
@Deprecated("Do not use - convert to layer")
abstract class AbstractDomainRelativePainter
protected constructor(
  override val calculator: ChartCalculator,
  snapXValues: Boolean,
  snapYValues: Boolean
) : AbstractPainter(snapXValues, snapYValues), DomainRelativePainter
