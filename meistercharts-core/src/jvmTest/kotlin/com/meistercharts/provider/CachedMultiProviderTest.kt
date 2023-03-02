package com.meistercharts.provider

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.SizedProvider
import it.neckar.open.provider.cached
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

/**
 *
 */
class CachedMultiProviderTest {
  @Test
  fun testCache() {
    val delegate: SizedProvider<TextKey?> = object : SizedProvider<TextKey?> {
      override fun size(): Int = 4

      override fun valueAt(index: Int): it.neckar.open.i18n.TextKey? {
        return it.neckar.open.i18n.TextKey.simple(index.toString())
      }
    }

    val cachedLabelsProvider = delegate.cached(3)

    assertThat(cachedLabelsProvider.size()).isEqualTo(4)
    assertThat(cachedLabelsProvider.valueAt(0)).isEqualTo(it.neckar.open.i18n.TextKey.simple("0"))
    assertThat(cachedLabelsProvider.valueAt(1)).isEqualTo(it.neckar.open.i18n.TextKey.simple("1"))
    assertThat(cachedLabelsProvider.valueAt(2)).isSameAs(cachedLabelsProvider.valueAt(2))

    assertThat(cachedLabelsProvider.cache.size).isEqualTo(3)
  }

  @Test
  fun testCacheNullable() {
    val delegate: SizedProvider<TextKey?> = object : SizedProvider<TextKey?> {
      override fun size(): Int = 4

      override fun valueAt(index: Int): it.neckar.open.i18n.TextKey? {
        if (index == 0) {
          return null
        }

        return it.neckar.open.i18n.TextKey.simple(index.toString())
      }
    }

    val cachedLabelsProvider = delegate.cached(3)

    assertThat(cachedLabelsProvider.size()).isEqualTo(4)
    assertThat(cachedLabelsProvider.valueAt(0)).isNull()
    assertThat(cachedLabelsProvider.valueAt(1)).isEqualTo(it.neckar.open.i18n.TextKey.simple("1"))
    assertThat(cachedLabelsProvider.valueAt(2)).isSameAs(cachedLabelsProvider.valueAt(2))

    assertThat(cachedLabelsProvider.cache.size).isEqualTo(2)
  }
}
