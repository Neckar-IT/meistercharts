package it.neckar.open.i18n.resolve

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.Locale
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextResolver

/**
 * A [TextResolver] that takes locales into account
 */
open class MapBasedTextResolver : TextResolver {
  private val locale2Key2Text = mutableMapOf<Locale, MutableMap<TextKey, String?>>()

  override fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String? {
    return locale2Key2Text[i18nConfiguration.textLocale]?.get(key)
  }

  /**
   * Sets the text for the given key an locale
   * @param locale the target locale
   * @param key the key that identifies the entry to be updated
   * @param text the text to be used; pass `null` to remove the entry
   */
  fun setText(locale: Locale, key: TextKey, text: String?) {
    if (text == null) {
      locale2Key2Text[locale]?.remove(key)
    } else {
      if (locale2Key2Text[locale] == null) {
        locale2Key2Text[locale] = mutableMapOf()
      }
      locale2Key2Text[locale]?.set(key, text)
    }
  }

  /**
   * Sets a bunch of texts for various locales
   */
  fun setTexts(texts: Map<Locale, Map<TextKey, String?>>) {
    texts.forEach { entry ->
      entry.value.forEach {
        setText(entry.key, it.key, it.value)
      }
    }
  }
}
