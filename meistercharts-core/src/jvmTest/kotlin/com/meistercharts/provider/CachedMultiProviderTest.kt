/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.provider

import assertk.*
import assertk.assertions.*
import it.neckar.open.i18n.TextKey
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.cached
import org.junit.jupiter.api.Test

/**
 *
 */
class CachedMultiProviderTest {
  @Test
  fun testCache() {
    val delegate: MultiProvider<String, TextKey?> = MultiProvider<String, TextKey?> { index -> TextKey.simple(index.toString()) }

    val cachedLabelsProvider = delegate.cached(3)

    assertThat(cachedLabelsProvider.cache.size).isEqualTo(0)
    assertThat(cachedLabelsProvider.valueAt(0)).isEqualTo(TextKey.simple("0"))
    assertThat(cachedLabelsProvider.cache.size).isEqualTo(1)
    assertThat(cachedLabelsProvider.valueAt(1)).isEqualTo(TextKey.simple("1"))
    assertThat(cachedLabelsProvider.cache.size).isEqualTo(2)
    assertThat(cachedLabelsProvider.valueAt(2)).isSameAs(cachedLabelsProvider.valueAt(2))
    assertThat(cachedLabelsProvider.cache.size).isEqualTo(3)
  }

  @Test
  fun testCacheNullable() {
    val delegate: MultiProvider<String, TextKey?> = MultiProvider<String, TextKey?> { index ->
      if (index == 0) {
        null
      } else {
        TextKey.simple(index.toString())
      }
    }

    val cachedLabelsProvider = delegate.cached(3)

    assertThat(cachedLabelsProvider.valueAt(0)).isNull()
    assertThat(cachedLabelsProvider.valueAt(1)).isEqualTo(TextKey.simple("1"))
    assertThat(cachedLabelsProvider.valueAt(2)).isSameAs(cachedLabelsProvider.valueAt(2))

    assertThat(cachedLabelsProvider.cache.size).isEqualTo(2)
  }
}
