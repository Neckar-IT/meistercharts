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
package com.meistercharts.axis

import it.neckar.open.provider.MultiProvider
import kotlin.jvm.JvmInline

/**
 * Represents a global time index. The index is always the same - depending on the resolution.
 * This index can be used to choose a color for a tick/segment/...
 */
@JvmInline
value class GlobalTimeIndex(val value: Int)

inline fun <T> MultiProvider<GlobalTimeIndex, T>.valueAt(index: GlobalTimeIndex): T {
  return this.valueAt(index.value)
}

