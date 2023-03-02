package com.meistercharts.provider

import it.neckar.open.provider.MultiProvider2
import it.neckar.open.provider.SizedProvider2
import it.neckar.open.i18n.I18nConfiguration
import it.neckar.open.i18n.TextKey
import it.neckar.open.i18n.TextService
import it.neckar.open.i18n.resolve
import it.neckar.open.unit.other.Index


/**
 * Provides labels for a text services and i18n configuration
 */
typealias LabelsProvider<IndexContext> = MultiProvider2<IndexContext, String, TextService, I18nConfiguration>

/**
 * Returns only empty strings ("")
 */
val EmptyStrings: LabelsProvider<Index> = LabelsProvider { _, _, _ -> "" }

/**
 * Provides labels - with a given size
 */
typealias SizedLabelsProvider = SizedProvider2<String, TextService, I18nConfiguration>

/**
 * Returns a labels provider that resolves the provided text keys
 */
fun SizedProvider2.Companion.forList(values: List<TextKey>): SizedLabelsProvider {
  return object : SizedProvider2<String, TextService, I18nConfiguration> {
    override fun size(param1: TextService, param2: I18nConfiguration): Int {
      return values.size
    }

    override fun valueAt(index: Int, param1: TextService, param2: I18nConfiguration): String {
      return values[index].resolve(param1, param2)
    }
  }
}

/**
 * Resolves the text keys
 */
fun SizedProvider2.Companion.forTextKeys(values: List<TextKey>): SizedLabelsProvider {
  return object : SizedLabelsProvider {
    override fun size(param1: TextService, param2: I18nConfiguration): Int {
      return values.size
    }

    override fun valueAt(index: Int, textService: TextService, i18nConfiguration: I18nConfiguration): String {
      return values[index].resolve(textService, i18nConfiguration)
    }
  }
}
