package it.neckar.open.i18n

import it.neckar.open.time.TimeZone

/**
 * I18n support.
 *
 * For each canvas support there exists one [I18nSupport]
 *
 * The locales may be changed at runtime, but there is no
 * guarantee that the new locale will be picked up by existing instances.
 * It is strongly suggested to set the locale *first* and instantiate any objects later
 */
class I18nSupport {
  /**
   * The current i18n configuration.
   * Is initialized with [DefaultI18nConfiguration]
   */
  var configuration: I18nConfiguration = DefaultI18nConfiguration

  /**
   * The time zone
   */
  var timeZone: TimeZone
    get() {
      return configuration.timeZone
    }
    set(value) {
      configuration = configuration.copy(timeZone = value)
    }

  /**
   * The locale that is used to resolve texts
   */
  var textLocale: Locale
    get() = configuration.textLocale
    set(value) {
      configuration = configuration.copy(textLocale = value)
    }

  /**
   * The locale that is used to format numbers / dates
   */
  var formatLocale: Locale
    get() = configuration.formatLocale
    set(value) {
      configuration = configuration.copy(formatLocale = value)
    }
}

/**
 * The default locale as provided by the system.
 * This value can not be changed since it is provided by the system
 */
val DefaultSystemLocale: Locale = DefaultLocaleProvider().defaultLocale

/**
 * Contains the default timeZone for this system.
 * Can not be set since it is provided by the system.
 *
 * The default time zone can be set for each chart in [it.neckar.open.i18n.I18nSupport]
 *
 * A custom default time zone can be configured when initializing the platform
 */
val DefaultSystemTimeZone: TimeZone = SystemTimeZoneProvider().systemTimeZone

/**
 * The default i18n configuration. Is used as default when initializing a new chart
 * This value can be changed! Use with care!
 */
var DefaultI18nConfiguration: I18nConfiguration = I18nConfiguration(
  textLocale = DefaultSystemLocale,
  formatLocale = DefaultSystemLocale,
  timeZone = DefaultSystemTimeZone
)
  private set

/**
 * Updates the [DefaultI18nConfiguration]. Use with care!
 * It is possible to set the locale for a chart itself ([I18nSupport]).
 */
fun updateDefaultI18nConfiguration(i18nConfiguration: I18nConfiguration) {
  DefaultI18nConfiguration = i18nConfiguration
}

/**
 * Sets this configuration as default
 */
fun I18nConfiguration.setAsDefault() {
  updateDefaultI18nConfiguration(this)
}
