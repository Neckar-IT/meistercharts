package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class HistoryConfigurationTest {
  @Test
  fun testBuilder() {
    val config = historyConfiguration {
      decimalDataSeries(DataSeriesId(7), TextKey.simple("Pressure 1"))
      decimalDataSeries(DataSeriesId(8), TextKey.simple("Pressure 2"))
      decimalDataSeries(DataSeriesId(9), TextKey.simple("Pressure 3"))
    }

    assertThat(config.decimalDataSeriesCount).isEqualTo(3)
  }

  @Test
  fun testCreation() {
    val historyConfiguration = historyConfiguration(3, 2, decimalDataSeriesInitializer = { dataSeriesIndex ->
      val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 100)
      decimalDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), HistoryUnit.None)
    }, enumDataSeriesInitializer = { dataSeriesIndex ->
      val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 1000)
      enumDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), HistoryEnum.Boolean)
    })

    assertThat(historyConfiguration.decimalDataSeriesCount).isEqualTo(3)
    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(2)
    assertThat(historyConfiguration.totalDataSeriesCount).isEqualTo(5)
  }

  @Test
  fun testReferenceEntry() {
    val historyConfiguration = historyConfiguration(
      decimalDataSeriesCount = 3,
      enumDataSeriesCount = 2,
      referenceEntrySeriesCount = 4,
      decimalDataSeriesInitializer = { dataSeriesIndex ->
        val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 100)
        decimalDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), HistoryUnit.None)
      },
      enumDataSeriesInitializer = { dataSeriesIndex ->
        val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 1000)
        enumDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), HistoryEnum.Boolean)
      },
      referenceEntryDataSeriesInitializer = { dataSeriesIndex ->
        val dataSeriesId = DataSeriesId(dataSeriesIndex.value * 10)
        referenceEntryDataSeries(dataSeriesId, TextKey.simple("DS.$dataSeriesId"), ReferenceEntriesDataMap.generated)
      }
    )

    assertThat(historyConfiguration.decimalDataSeriesCount).isEqualTo(3)
    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(2)
    assertThat(historyConfiguration.referenceEntryDataSeriesCount).isEqualTo(4)

    assertThat(historyConfiguration.totalDataSeriesCount).isEqualTo(3 + 2 + 4)
  }
}
