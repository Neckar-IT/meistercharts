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
package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.historyConfigurationOnlyDecimals
import it.neckar.open.i18n.TextKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows


/**
 */
class HistoryChunkBuilderTest {
  @Test
  fun testResize() {
    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(1), TextKey.simple("1"))
      decimalDataSeries(DataSeriesId(2), TextKey.simple("2"))
    }

    val chunk = historyConfiguration.chunk {
      this.historyValuesBuilder.resizeTimestamps(1)

      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(1)
      addDecimalValues(77.0, 1.0, 2.0)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(1)

      //resize automatically
      addDecimalValues(78.0, 10.0, 20.0)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(2)

      addDecimalValues(79.0, 100.0, 200.0)
      assertThat(historyValuesBuilder.timestampsCount).isEqualTo(4)
    }

    assertThat(chunk.timeStampsCount).isEqualTo(3)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(2.0)
    assertThat(chunk.timestampCenter(TimestampIndex(0))).isEqualTo(77.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(100.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(2))).isEqualTo(200.0)
    assertThat(chunk.timestampCenter(TimestampIndex(2))).isEqualTo(79.0)
  }

  @Test
  fun testBuilderValidation() {
    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(1), TextKey.simple("1"))
      decimalDataSeries(DataSeriesId(3), TextKey.simple("2"))
      decimalDataSeries(DataSeriesId(4), TextKey.simple("3"))
      decimalDataSeries(DataSeriesId(5), TextKey.simple("4"))
    }

    val builder = HistoryChunkBuilder(historyConfiguration, expectedTimestampsCount = 7)

    assertThrows<IllegalStateException> {
      builder.addDecimalValues(Double.NaN, 17.0, 18.0, 20.0, 21.0)
    }

    //Invalid number of significands
    assertThrows<IllegalArgumentException> {
      builder.addDecimalValues(1234.0, 17.0)
    }.let {
      assertThat(it.message).isNotNull().startsWith("Invalid values count")
    }
  }

  @Test
  fun testBuilder() {
    val historyConfiguration = historyConfigurationOnlyDecimals(5) { dataSeriesIndex ->
      decimalDataSeries(DataSeriesId((dataSeriesIndex.value + 1) * 2), TextKey.simple("Label $dataSeriesIndex"))
    }

    val chunk = historyConfiguration.chunk(7) { timestampIndex ->
      addDecimalValues(100.000 + timestampIndex.value * 100.0) { dataSeriesIndex: DecimalDataSeriesIndex ->
        (dataSeriesIndex.value + 1.0) * (timestampIndex.value + 1)
      }
    }

    assertThat(chunk.configuration.decimalConfiguration.dataSeriesIds).hasSize(5)
    assertThat(chunk.timeStamps).hasSize(7)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(3))).isEqualTo(12.0)
  }

  @Test
  fun testSetSpecialValues() {
    val historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(77), TextKey.simple("Label"))
    }

    val timestampsCount = 5
    val chunk = HistoryChunkBuilder(historyConfiguration, expectedTimestampsCount = timestampsCount).apply {
      addDecimalValues(0.0, Double.NaN)
      addDecimalValues(1.0, Double.POSITIVE_INFINITY)
      addDecimalValues(2.0, Double.NEGATIVE_INFINITY)
      addDecimalValues(3.0, Int.MAX_VALUE + 1.0)
      addDecimalValues(4.0, Int.MIN_VALUE - 1.0)
    }.build()

    assertThat(chunk.timeStampsCount).isEqualTo(5)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(Double.NaN)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(Double.POSITIVE_INFINITY)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(Double.NEGATIVE_INFINITY)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(Int.MAX_VALUE + 1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(4))).isEqualTo(Int.MIN_VALUE - 1.0)
  }
}
