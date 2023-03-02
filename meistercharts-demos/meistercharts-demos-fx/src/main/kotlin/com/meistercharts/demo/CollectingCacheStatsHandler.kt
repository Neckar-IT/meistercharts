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
