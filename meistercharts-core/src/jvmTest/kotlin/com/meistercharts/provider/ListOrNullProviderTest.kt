package com.meistercharts.provider

import assertk.*
import assertk.assertions.*
import it.neckar.open.provider.MultiProvider
import it.neckar.open.provider.MultiProviderIndexContextAnnotation
import org.junit.jupiter.api.Test

/**
 *
 */
class ListOrNullProviderTest {
  @Test
  fun testNull() {
    val provider: MultiProvider<MyProviderContextInfo, String?> = MultiProvider.alwaysNull()
    assertThat(provider.valueAt(17)).isNull()
  }

  @Test
  fun testEmptyList() {
    val provider = MultiProvider.forListOrNull<MyProviderContextInfo, String>(listOf<String>())

    assertThat(provider.valueAt(1717)).isNull()
    assertThat(provider.valueAt(0)).isNull()
  }

  @Test
  fun testListOrNull() {
    val provider = MultiProvider.forListOrNull<MyProviderContextInfo, String>(listOf("a", "b", "c"))

    assertThat(provider.valueAt(0)).isEqualTo("a")
    assertThat(provider.valueAt(2)).isEqualTo("c")
    assertThat(provider.valueAt(3)).isEqualTo(null)
  }
}

@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
@MultiProviderIndexContextAnnotation
annotation class MyProviderContextInfo
