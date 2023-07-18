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
package com.meistercharts.label

import com.meistercharts.color.Color
import com.meistercharts.annotations.DomainRelative
import it.neckar.open.unit.other.pct

/**
 * Represents a label with domain value
 */
@Deprecated("No longer used")
data class DomainRelativeLabel(
  /**
   * The domain relative value
   */
  @pct
  @DomainRelative val value: Double,

  /**
   * The data for the label
   */
  val labelData: LabelData
) {

  fun withColor(newColor: Color): DomainRelativeLabel {
    return DomainRelativeLabel(value, labelData.withColor(newColor))
  }
}
