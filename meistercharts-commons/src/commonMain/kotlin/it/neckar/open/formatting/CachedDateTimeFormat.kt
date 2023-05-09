package it.neckar.open.formatting

import it.neckar.open.collections.cache
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import kotlin.jvm.JvmOverloads

/**
 * A format that returns cached values.
 *
 * This interface should be used at declarations (e.g. in Styles) to ensure a cache is used
 *
 */
interface CachedDateTimeFormat : DateTimeFormat {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A format that caches the results
 */
class DefaultCachedDateTimeFormat @JvmOverloads constructor(
  val format: DateTimeFormat,
  /**
   * The maximum size of the cache
   */
  val cacheSize: Int = 500
) : CachedDateTimeFormat {

  init {
    require(format !is CachedNumberFormat) { "cannot cache an already cached dateTime format" }
  }

  /**
   * The cache for the "normal" formatted strings
   */
  private val formatCache = cache<Int, String>("DefaultCachedDateTimeFormat", cacheSize)

  /**
   * Returns the size of the cache
   */
  override val currentCacheSize: Int
    get() = formatCache.size

  override fun format(timestamp: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val key = 31 * timestamp.hashCode() + 17 * i18nConfiguration.hashCode() + 18 * whitespaceConfig.hashCode()

    return formatCache.getOrStore(key) {
      format.format(timestamp, i18nConfiguration, whitespaceConfig)
    }
  }
}

/**
 * Caches the results of the dateTime format
 */
fun DateTimeFormat.cached(cacheSize: Int = 100): CachedDateTimeFormat {
  return DefaultCachedDateTimeFormat(this, cacheSize = cacheSize)
}

