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
package com.meistercharts.demo.descriptors.zeitstrahl

sealed interface DeepTimeTickStep {
  /** The mean length, which selects the step for a zoom level. */
  val approximateSeconds: Double

  data class Hours(val hours: Int) : DeepTimeTickStep {
    override val approximateSeconds: Double
      get() = hours * ProlepticGregorianCalendar.SecondsPerHour
  }

  data class Days(val days: Int) : DeepTimeTickStep {
    override val approximateSeconds: Double
      get() = days * ProlepticGregorianCalendar.SecondsPerDay
  }

  data class Months(val months: Int) : DeepTimeTickStep {
    override val approximateSeconds: Double
      get() = months * ProlepticGregorianCalendar.SecondsPerYear / 12
  }

  data class Years(val years: Long) : DeepTimeTickStep {
    override val approximateSeconds: Double
      get() = years * ProlepticGregorianCalendar.SecondsPerYear
  }
}
