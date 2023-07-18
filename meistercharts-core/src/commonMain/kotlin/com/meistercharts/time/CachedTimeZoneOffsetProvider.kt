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

/**
 * A time-zone-offset provider that returns cached values.
 */
interface CachedTimeZoneOffsetProvider : TimeZoneOffsetProvider {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * Creates a [CachedTimeZoneOffsetProvider] with this [TimeZoneOffsetProvider] being its delegate
 */
fun TimeZoneOffsetProvider.cached(cacheSize: Int = 500): CachedTimeZoneOffsetProvider {
  return DefaultCachedTimeZoneOffsetProvider(this, cacheSize)
}
