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
package com.meistercharts.calc

import com.meistercharts.annotations.Domain
import com.meistercharts.annotations.DomainRelative
import com.meistercharts.range.ValueRange
import it.neckar.open.unit.number.MayBeNegative
import it.neckar.open.unit.si.rad

/**
 * Converts a domain value to an angle in radians.
 *
 * This function maps a domain value to an angular position, useful for circular charts like pie or gauge charts.
 * The angle is calculated by converting the domain value to a relative position (0.0 to 1.0) within the value range,
 * then scaling it by the rotation extent and adding it to the start angle.
 */
fun domain2rad(
  value: @Domain Double,
  valueRange: ValueRange,
  startAngle: @rad Double,
  extendWithRotationDirection: @rad @MayBeNegative Double,
): @rad Double {
  @DomainRelative val domainRelative = valueRange.toDomainRelative(value)
  return (startAngle + domainRelative * extendWithRotationDirection)
}
