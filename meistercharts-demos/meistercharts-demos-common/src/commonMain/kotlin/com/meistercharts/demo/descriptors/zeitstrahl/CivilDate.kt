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

/**
 * A day of the proleptic Gregorian calendar with astronomical year numbering: year 0 is 1 BC.
 */
data class CivilDate(
  val year: Long,
  val month: Int,
  val day: Int,
) {
  init {
    require(month in 1..12) { "Month <$month> lies outside 1..12" }
    require(day in 1..ProlepticGregorianCalendar.daysInMonth(year, month)) { "Day <$day> lies outside the month $year-$month" }
  }
}
