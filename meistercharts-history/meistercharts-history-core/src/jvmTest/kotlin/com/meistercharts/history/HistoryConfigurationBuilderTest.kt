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
      referenceEntryDataSeries(DataSeriesId(7), "ds7", HistoryEnum.Active)
      referenceEntryDataSeries(DataSeriesId(8), "ds8", HistoryEnum.Boolean)
    }

    assertThat(configuration.referenceEntryConfiguration.dataSeriesIds).hasSize(2)
    assertThat(configuration.referenceEntryConfiguration.dataSeriesIds[1]).isEqualTo(8)

    assertThat(configuration.referenceEntryConfiguration.getStatusEnum(ReferenceEntryDataSeriesIndex.zero)?.enumDescription).isEqualTo("Active")
    assertThat(configuration.referenceEntryConfiguration.getStatusEnum(ReferenceEntryDataSeriesIndex(1))?.enumDescription).isEqualTo("Boolean")
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
        referenceEntryDataSeries(DataSeriesId(dataSeriesIndex.value), "Enum DS $dataSeriesIndex", HistoryEnum.Boolean)
      }
    )

    assertThat(historyConfiguration.totalDataSeriesCount).isEqualTo(2 + 3 + 4)
  }
}
