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
 * A time window of a [WindowLoader]; [key] names everything besides the window, such as the zoom level of a lane or the ID of a periodization.
 */
data class WindowRequest<K>(
  val key: K,
  val from: Double,
  val to: Double,
) {
  fun covers(key: K, from: Double, to: Double): Boolean = this.key == key && this.from <= from && to <= this.to
}
