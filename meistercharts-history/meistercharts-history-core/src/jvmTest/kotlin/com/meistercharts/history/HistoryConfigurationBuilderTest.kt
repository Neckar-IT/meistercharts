package com.meistercharts.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test

class HistoryConfigurationBuilderTest {
  @Test
  fun testDecimals() {
    val configuration = historyConfiguration {
      decimalDataSeries(DataSeriesId(7), TextKey.simple("ds7"))
      decimalDataSeries(DataSeriesId(8), TextKey.simple("ds8"))
    }

    assertThat(configuration.decimalConfiguration.dataSeriesIds).hasSize(2)
    assertThat(configuration.decimalConfiguration.dataSeriesIds[1]).isEqualTo(8)
  }

  @Test
  fun testBuildEnums() {
    val configuration = historyConfiguration {
      enumDataSeries(DataSeriesId(7), "ds7", HistoryEnum.create("test1", listOf(TextKey.simple("a"))))
      enumDataSeries(DataSeriesId(8), "ds8", HistoryEnum.create("test2", listOf(TextKey.simple("foo"))))
    }

    assertThat(configuration.enumConfiguration.dataSeriesIds).hasSize(2)
    assertThat(configuration.enumConfiguration.dataSeriesIds[1]).isEqualTo(8)
  }

  @Test
  fun testRefs() {
    val configuration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(7), "ds7", ReferenceEntriesDataMap.generated)
      referenceEntryDataSeries(DataSeriesId(8), "ds8", ReferenceEntriesDataMap.generated)
    }

    assertThat(configuration.referenceEntryConfiguration.dataSeriesIds).hasSize(2)
    assertThat(configuration.referenceEntryConfiguration.dataSeriesIds[1]).isEqualTo(8)
  }

  @Test
  fun testAll() {
    val historyConfiguration = historyConfiguration(
      decimalDataSeriesCount = 2,
      enumDataSeriesCount = 3,
      referenceEntrySeriesCount = 4,
      decimalDataSeriesInitializer = { dataSeriesIndex: DecimalDataSeriesIndex ->
        decimalDataSeries(DataSeriesId(dataSeriesIndex.value * 10), "DN: $dataSeriesIndex")
      },
      enumDataSeriesInitializer = { dataSeriesIndex: EnumDataSeriesIndex ->
        enumDataSeries(DataSeriesId(dataSeriesIndex.value), "Enum DS $dataSeriesIndex", HistoryEnum.Boolean)
      },
      referenceEntryDataSeriesInitializer = { dataSeriesIndex ->
        referenceEntryDataSeries(DataSeriesId(dataSeriesIndex.value), "Enum DS $dataSeriesIndex", ReferenceEntriesDataMap.generated)
      }
    )

    assertThat(historyConfiguration.totalDataSeriesCount).isEqualTo(2 + 3 + 4)
  }
}
