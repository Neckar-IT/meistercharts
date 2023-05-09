package it.neckar.open.i18n.resolve

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextResolver

/**
 * Returns the text key as text.
 * Used as fallback
 */
object TextKeyTextResolver : TextResolver {
  override fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String? {
    return key.key
  }
}
