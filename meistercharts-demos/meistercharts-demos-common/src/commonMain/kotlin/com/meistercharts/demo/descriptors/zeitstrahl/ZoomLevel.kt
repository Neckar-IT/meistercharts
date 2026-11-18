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

import kotlin.jvm.JvmInline
import kotlin.math.pow

/**
 * `floor(log2(secondsPerPixel))`: 1 shows 2 s per pixel, 48 the whole universe on 1500 px; `zeitstrahl-poc-server` accepts the same range.
 */
@JvmInline
value class ZoomLevel(val value: Int) {
  init {
    require(value in MinValue..MaxValue) { "Zoom level <$value> lies outside $MinValue..$MaxValue" }
  }

  /** An entry or section of 16 tiles of `2^(z+10)` seconds and more is hidden on this level. */
  val maxDurationSeconds: Double
    get() = 16 * 2.0.pow(value + 10)

  override fun toString(): String = value.toString()

  companion object {
    const val MinValue: Int = 1
    const val MaxValue: Int = 48
  }
}
