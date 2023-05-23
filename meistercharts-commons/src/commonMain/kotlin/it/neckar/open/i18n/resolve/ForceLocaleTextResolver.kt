package it.neckar.open.i18n.resolve

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextResolver

/**
 * Returns the translation for a defined locale
 */
class ForceLocaleTextResolver(
  var locale: Locale,
  val delegate: TextResolver
) : TextResolver {
  override fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String? {
    return delegate.resolve(key, i18nConfiguration.copy(textLocale = locale)) //Do *not* use the requested locale but the fallback locale instead
  }
}
