package it.neckar.open.formatting

import it.neckar.datetime.minimal.LocalDate
import it.neckar.open.collections.cache
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig
import kotlin.jvm.JvmOverloads

/**
 * A format that returns cached values.
 *
 * This interface should be used at declarations (e.g. in Styles) to ensure a cache is used
 */
interface CachedLocalDateFormat : LocalDateFormat {
  /**
   * Returns the current cache size
   */
  val currentCacheSize: Int
}

/**
 * A format that caches the results
 */
class DefaultCachedLocalDateFormat @JvmOverloads constructor(
  val format: LocalDateFormat,
  /**
   * The maximum size of the cache
   */
  val cacheSize: Int = 500
) : CachedLocalDateFormat {

  init {
    require(format !is CachedNumberFormat) { "cannot cache an already cached localDate format" }
  }

  /**
   * The cache for the "normal" formatted strings
   */
  private val formatCache = cache<Int, String>("DefaultCachedLocalDateFormat", cacheSize)

  /**
   * Returns the size of the cache
   */
  override val currentCacheSize: Int
    get() = formatCache.size

  override fun format(localDate: LocalDate, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val key = 31 * localDate.hashCode() + 17 * i18nConfiguration.hashCode() + 18 * whitespaceConfig.hashCode()

    return formatCache.getOrStore(key) {
      format.format(localDate, i18nConfiguration, whitespaceConfig)
    }
  }
}

/**
 * Caches the results of the localDate format
 */
fun LocalDateFormat.cached(cacheSize: Int = 100): CachedLocalDateFormat {
  return DefaultCachedLocalDateFormat(this, cacheSize = cacheSize)
}


