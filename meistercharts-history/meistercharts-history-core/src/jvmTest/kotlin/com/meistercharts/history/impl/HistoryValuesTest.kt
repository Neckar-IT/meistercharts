package com.meistercharts.history.impl

import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.HistoryChunk.Companion.Pending
import it.neckar.open.serialization.roundTrip
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test

/**
 */
class HistoryValuesTest {
  @Test
  fun testSerialization() {
    val builder = HistoryValuesBuilder(7, 0, 0, 12, RecordingType.Measured)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0), 12.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1), 13.0)

    val historyValues = builder.build()

    roundTrip(historyValues) {
      //language=JSON
      """
        {
          "decimalHistoryValues" : {
            "values" : "AAcADEAoAAAAAAAAf+////////9/7////////3/v////////f+////////9/7////////3/v////////QCoAAAAAAAB/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////3/v////////f+////////9/7////////w==",
            "minValues" : null,
            "maxValues" : null
          },
          "enumHistoryValues" : {
            "values" : "AAAADA==",
            "mostOfTheTimeValues" : null
          },
          "referenceEntryHistoryValues" : {
            "values" : "AAAADA==",
            "differentIdsCount" : null,
            "dataMaps" : [ ]
          }
        }
      """.trimIndent()
    }
  }

  @Test
  internal fun testIt() {
    val builder = HistoryValuesBuilder(7, 0, 0, 12, RecordingType.Measured)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0), 12.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1), 13.0)

    val historyValues = builder.build()

    assertThat(historyValues.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(12.0)
    assertThat(historyValues.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(13.0)
  }

  @Test
  fun testSetValues() {
    val builder = HistoryValuesBuilder(2, 0, 0, 3, RecordingType.Measured)

    builder.setDecimalValuesForTimestamp(TimestampIndex(0), doubleArrayOf(7.0, 70.0), null, null)
    builder.setDecimalValuesForTimestamp(TimestampIndex(1), doubleArrayOf(8.0, 71.0), null, null)
    builder.setDecimalValuesForTimestamp(TimestampIndex(2), doubleArrayOf(9.0, 72.0), null, null)

    builder.build().let {
      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(7.0)
      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(70.0)

      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(8.0)
      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(71.0)

      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(9.0)
      assertThat(it.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(2))).isEqualTo(72.0)
    }
  }

  @Test
  fun testGetValues() {
    val builder = HistoryValuesBuilder(7, 0, 0, 12, RecordingType.Measured)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0), 12.0)

    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1), 13.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1), 14.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1), 16.0)

    val historyValues = builder.build()
    historyValues.getDecimalValues(TimestampIndex(0)).let {
      assertThat(it).hasSize(7)
      assertThat(it).containsExactly(12.0, Pending, Pending, Pending, Pending, Pending, Pending)
    }

    historyValues.getDecimalValues(TimestampIndex(1)).let {
      assertThat(it).hasSize(7)
      assertThat(it).containsExactly(13.0, 14.0, Pending, 16.0, Pending, Pending, Pending)
    }
  }

  @Test
  fun testEquals() {
    val builder = HistoryValuesBuilder(7, 0, 0, 12, RecordingType.Measured)
    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0), 12.0)

    builder.setDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1), 13.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1), 14.0)
    builder.setDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1), 16.0)

    assertThat(builder.build()).isEqualTo(builder.build())
  }
}
