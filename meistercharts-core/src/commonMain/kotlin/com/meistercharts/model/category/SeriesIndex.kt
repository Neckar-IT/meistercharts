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
package com.meistercharts.model.category

import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 *
 */
@JvmInline
value class SeriesIndex(val value: Int) {

  companion object {
    val zero: SeriesIndex = SeriesIndex(0)
    val one: SeriesIndex = SeriesIndex(1)
  }
}

inline fun <T> MultiProvider<SeriesIndex, T>.valueAt(index: SeriesIndex): T {
  return this.valueAt(index.value)
}
