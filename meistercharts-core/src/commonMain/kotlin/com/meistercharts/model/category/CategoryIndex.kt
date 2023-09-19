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
@file:Suppress("NOTHING_TO_INLINE")

package com.meistercharts.model.category

import it.neckar.open.provider.MultiDoublesProvider
import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 * Represents a category index
 */
@JvmInline
value class CategoryIndex(val value: Int) {
  val isFirst: Boolean
    get() {
      return value == 0
    }

  fun isEqual(other: Int): Boolean {
    return value == other
  }

  companion object {
    val zero: CategoryIndex = CategoryIndex(0)
    val one: CategoryIndex = CategoryIndex(1)
  }
}

inline fun <T> MultiProvider<CategoryIndex, T>.valueAt(index: CategoryIndex): T {
  return this.valueAt(index.value)
}

inline fun MultiDoublesProvider<CategoryIndex>.valueAt(index: CategoryIndex): Double {
  return this.valueAt(index.value)
}
