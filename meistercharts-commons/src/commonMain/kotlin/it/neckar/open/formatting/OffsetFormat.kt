package it.neckar.open.formatting

import it.neckar.open.annotations.JavaFriendly
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.kotlin.lang.WhitespaceConfig

/**
 * Formats a value adding an offset to it
 */
class OffsetFormat(
  /**
   * The delegate that is used to format the offset value
   */
  val delegate: CachedNumberFormat,
  /**
   * Provides the offset that is added to the value before formatting using the delegate
   */
  val offsetProvider: () -> Double
) : CachedNumberFormat {
  @JavaFriendly
  constructor(
    delegate: CachedNumberFormat,
    offsetProvider: OffsetProvider
  ) : this(delegate, offsetProvider::offset)

  override val currentCacheSize: Int
    get() = delegate.currentCacheSize

  override fun format(value: Double, i18nConfiguration: I18nConfiguration, whitespaceConfig: WhitespaceConfig): String {
    val offset = offsetProvider()
    return delegate.format(value + offset, i18nConfiguration, whitespaceConfig)
  }

  override val precision: Double
    get() = delegate.precision
}

/**
 * Provides the offset
 */
@JavaFriendly
interface OffsetProvider {
  /**
   * Returns the offset
   */
  val offset: Double
}
