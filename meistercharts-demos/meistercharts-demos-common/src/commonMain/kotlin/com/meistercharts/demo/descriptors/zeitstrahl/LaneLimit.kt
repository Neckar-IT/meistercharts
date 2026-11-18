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

/**
 * The number of entries a lane asks `zeitstrahl-poc-server` for; the server refuses more than [MaxValue].
 */
@JvmInline
value class LaneLimit(val value: Int) {
  init {
    require(value in 1..MaxValue) { "Lane limit <$value> lies outside 1..$MaxValue" }
  }

  companion object {
    const val MaxValue: Int = 100
  }
}
