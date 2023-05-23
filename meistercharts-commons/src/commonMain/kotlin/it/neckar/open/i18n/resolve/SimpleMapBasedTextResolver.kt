package it.neckar.open.i18n.resolve

import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextResolver

/**
 * A [TextResolver] that does not care for locales
 */
open class SimpleMapBasedTextResolver : TextResolver {
  private val key2Text = mutableMapOf<TextKey, String?>()

  override fun resolve(key: TextKey, i18nConfiguration: I18nConfiguration): String? {
    return key2Text[key]
  }

  /**
   * Sets the text for the given key.
   * @param key the key that identifies the entry to be updated
   * @param text the text to be used; pass `null` to remove the entry
   */
  fun setText(key: TextKey, text: String?) {
    if (text == null) {
      key2Text.remove(key)
    } else {
      key2Text[key] = text
    }
  }

  /**
   * Sets a bunch of texts.
   *
   * A text that is `null` results in removing the corresponding entry from this resolver.
   */
  fun setTexts(texts: Map<TextKey, String?>) {
    texts.forEach {
      setText(it.key, it.value)
    }
  }
}
