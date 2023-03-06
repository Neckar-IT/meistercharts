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
package com.meistercharts.demo

import it.neckar.open.collections.Cache
import it.neckar.open.collections.CacheStatsHandler
import it.neckar.open.collections.WeakMap
import it.neckar.commons.logback.debug
import it.neckar.logging.LoggerFactory

/**
 * Holds weak references to each cache
 */
class CollectingCacheStatsHandler : CacheStatsHandler {
  /**
   * Contains the caches and descriptions as keys
   */
  val caches: WeakMap<Cache<*, *>, String> = WeakMap()

  override fun <K, V> cacheCreated(description: String, cache: Cache<K, V>) {
    logger.debug { "Cache created <$description>" }
    caches[cache] = description
  }

  override fun <K, V> freed(description: String, k: K, v: V) {
  }

  companion object {
    private val logger = LoggerFactory.getLogger("com.meistercharts.demo.CollectingCacheStatsHandler")
  }
}
