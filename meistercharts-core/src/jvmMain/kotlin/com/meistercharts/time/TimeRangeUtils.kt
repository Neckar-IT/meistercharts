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
package com.meistercharts.time

import it.neckar.open.time.toDoubleMillis
import java.time.Instant
import java.time.chrono.ChronoZonedDateTime

/**
 * Creates a time range
 */
fun ChronoZonedDateTime<*>.timeRangeTo(to: ChronoZonedDateTime<*>): TimeRange {
  val fromMillis = toDoubleMillis()
  val toMillis = to.toDoubleMillis()

  return TimeRange(fromMillis, toMillis)
}

fun Instant.timeRangeTo(to: Instant): TimeRange {
  val fromMillis = toDoubleMillis()
  val toMillis = to.toDoubleMillis()

  return TimeRange(fromMillis, toMillis)
}

