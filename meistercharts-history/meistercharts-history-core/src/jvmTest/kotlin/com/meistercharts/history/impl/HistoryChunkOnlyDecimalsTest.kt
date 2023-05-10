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
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk.Companion.isNoValue
import com.meistercharts.history.impl.HistoryChunk.Companion.isPending
import com.meistercharts.history.impl.HistoryChunk.Companion.maxHistoryAware
import com.meistercharts.history.impl.HistoryChunk.Companion.minHistoryAware
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

/**
 */
class HistoryChunkOnlyDecimalsTest {
  lateinit var chunk: HistoryChunk

  lateinit var historyConfiguration: HistoryConfiguration


  @BeforeEach
  internal fun setUp() {
    historyConfiguration = historyConfiguration {
      decimalDataSeries(DataSeriesId(10), TextKey("temp", "Temperature"))
      decimalDataSeries(DataSeriesId(11), TextKey("height", "Height"))
      decimalDataSeries(DataSeriesId(12), TextKey("temp2", "Temperature 2"))
      decimalDataSeries(DataSeriesId(13), TextKey("temp3", "Temperature 3"))
    }

    chunk = historyConfiguration.chunk() {
      addDecimalValues(1001.0, 1.0, 10.0, 100.0, 1000.0)
      addDecimalValues(1002.0, 2.0, 20.0, 200.0, 2000.0)
      addDecimalValues(1003.0, 3.0, 30.0, 300.0, 3000.0)
    }

    assertThat(chunk).isNotNull()
  }

  @Test
  fun testSerialization() {
    roundTrip(chunk){
      //language=JSON
      """
        {
          "configuration" : {
            "decimalConfiguration" : {
              "dataSeriesIds" : [ 10, 11, 12, 13 ],
              "displayNames" : [ {
                "key" : "temp",
                "fallbackText" : "Temperature"
              }, {
                "key" : "height",
                "fallbackText" : "Height"
              }, {
                "key" : "temp2",
                "fallbackText" : "Temperature 2"
              }, {
                "key" : "temp3",
                "fallbackText" : "Temperature 3"
              } ],
              "units" : [ null, null, null, null ]
            },
            "enumConfiguration" : {
              "dataSeriesIds" : [ ],
              "displayNames" : [ ],
              "enums" : [ ]
            },
            "referenceEntryConfiguration" : {
              "dataSeriesIds" : [ ],
              "displayNames" : [ ],
              "statusEnums" : [ ]
            }
          },
          "timeStamps" : [ 1001.0, 1002.0, 1003.0 ],
          "values" : {
            "decimalHistoryValues" : {
              "values" : "AAQAAz/wAAAAAAAAQCQAAAAAAABAWQAAAAAAAECPQAAAAAAAQAAAAAAAAABANAAAAAAAAEBpAAAAAAAAQJ9AAAAAAABACAAAAAAAAEA+AAAAAAAAQHLAAAAAAABAp3AAAAAAAA==",
              "minValues" : null,
              "maxValues" : null
            },
            "enumHistoryValues" : {
              "values" : "AAAAAw==",
              "mostOfTheTimeValues" : null
            },
            "referenceEntryHistoryValues" : {
              "values" : "AAAAAw==",
              "differentIdsCount" : null,
              "statuses" : "AAAAAw==",
               "dataMap" : {
               "type" : "Default",
               "entries" : { }
              }
            }
          },
          "recordingType" : "Measured"
        }
      """.trimIndent()
    }
  }


  @Test
  fun testDumpNoTrailingSpaces() {
    val dump = chunk.dump()
    dump.lines().forEach { line ->
      assertThat(line.endsWith(" "), line).isFalse()
    }
  }

  @Test
  fun testDoubleSpecialValues() {
    assertThat(HistoryChunk.NoValue.isNoValue()).isTrue()
    assertThat(HistoryChunk.NoValue.isPending()).isFalse()

    assertThat(HistoryChunk.Pending.isPending()).isTrue()
    assertThat(HistoryChunk.Pending.isNoValue()).isFalse()

    assertThat(HistoryChunk.Pending).isEqualTo(HistoryChunk.Pending)
    assertThat(HistoryChunk.NoValue).isEqualTo(HistoryChunk.NoValue)
  }

  @Test
  fun testContains() {
    assertThat(chunk.containsAny(1001.0, 1003.0), "exact").isTrue()
    assertThat(chunk.containsAny(1000.0, 1001.0), "start").isFalse()
    assertThat(chunk.containsAny(1003.0, 1004.0), "top").isTrue()

    assertThat(chunk.containsAny(1000.0, 2000.0), "around all").isTrue()
    assertThat(chunk.containsAny(1000.0, 1002.0), "around start").isTrue()
    assertThat(chunk.containsAny(1002.0, 2000.0), "around end").isTrue()

    assertThat(chunk.containsAny(1001.0 - 0.00000001, 1001.0), "before, exact").isFalse()
    assertThat(chunk.containsAny(1000.0, 1001.0), "before, exact").isFalse()
    assertThat(chunk.containsAny(1000.0, 1000.5), "before, wide").isFalse()
    assertThat(chunk.containsAny(1000.0, 1001.0 + 0.00000001), "before, epsilon").isTrue()

    assertThat(chunk.containsAny(1003.0, 1003.5), "after, exact").isTrue()
    assertThat(chunk.containsAny(1003.0 + 0.00000001, 1003.5), "after, exact, epsilon").isFalse()
    assertThat(chunk.containsAny(1004.0, 1005.5), "after, wide").isFalse()
  }

  @Test
  fun testWithoutData() {
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    chunk.withoutValues().let {
      assertThat(it.timeStampsCount).isEqualTo(0)
      assertThat(it.decimalDataSeriesCount).isEqualTo(4)
    }
  }

  @Test
  fun testSubRange() {
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(0)

    assertThat(chunk.timeStampsCount).isEqualTo(3)


    assertThat(chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
    assertThat(chunk.firstTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")

    assertThat(chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
    assertThat(chunk.timestampCenter(TimestampIndex(2)).formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    assertThat(chunk.lastTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")

    chunk.range(0.0, chunk.timestampCenter(TimestampIndex(2)))!!.let {
      //contains everything
      assertThat(it.firstTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.lastTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
    }

    chunk.range(chunk.timestampCenter(TimestampIndex(0)), 999999999999.0)!!.let {
      //contains everything
      assertThat(it.firstTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.lastTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    }

    chunk.range(0.0, chunk.timestampCenter(TimestampIndex(1)))!!.let {
      //contains *not*
      assertThat(it.firstTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.lastTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
    }

    chunk.range(chunk.timestampCenter(TimestampIndex(1)), 999999999999999.0)!!.let {
      //contains *not*
      assertThat(it.firstTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
      assertThat(it.lastTimestamp.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    }
  }

  @Test
  fun testGetSignificands() {
    chunk.getDecimalValues(TimestampIndex(0)).let {
      assertThat(it).hasSize(historyConfiguration.decimalConfiguration.dataSeriesIds.size)
    }
  }

  @Test
  internal fun testValues() {
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(0)
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(11)).value).isEqualTo(1)
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(12)).value).isEqualTo(2)
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(13)).value).isEqualTo(3)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(2.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(20.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(0))).isEqualTo(100.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(200.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(0))).isEqualTo(1000.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1))).isEqualTo(2000.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(2.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(20.0)
  }

  @Test
  internal fun testFindBestIndex() {
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(chunk.bestTimestampIndexFor(1000.0).found).isFalse()
    assertThat(chunk.bestTimestampIndexFor(1000.0).index).isEqualTo(-1)
    assertThat(chunk.bestTimestampIndexFor(1000.0).raw).isEqualTo(-1)
    assertThat(chunk.bestTimestampIndexFor(1000.0).nearIndex).isEqualTo(0)
  }

  @Test
  fun testMergePartially() {
    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    val other = historyConfiguration.chunk() {
      addDecimalValues(2000.0, 7.0, 70.0, 700.0, 7000.0)
      addDecimalValues(5000.0, 8.0, 80.0, 800.0, 8000.0)
    }

    //Merge
    assertThat(chunk.timeStamps).isEqualTo(doubleArrayOf(1001.0, 1002.0, 1003.0))
    assertThat(other.timeStamps).isEqualTo(doubleArrayOf(2000.0, 5000.0))

    chunk.merge(other, 0.0, 10_000.0)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0, 2000.0, 5000.0)
    }

    //Merge none
    val merged = chunk.merge(other, 1.0, 2.0)
    assertThat(merged).isNull()

    //Exactly the old entries
    chunk.merge(other, 1000.0, 1500.0)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0)
    }

    //Merge all
    chunk.merge(other, 1000.0, 20_000.0)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0, 2000.0, 5000.0)
    }

    //Some 2
    chunk.merge(other, 1000.0, 2_000.0)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0)
    }

    chunk.merge(other, 1000.0, 2_000.0001)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0, 2000.0)
    }

    chunk.merge(other, 1001.0, 1009.0)!!.let { merged ->
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0)
    }
  }

  @Test
  fun testMergeOverlapping() {
    val otherDuplicates = historyConfiguration.chunk() {
      addDecimalValues(1002.0, 7.0, 70.0, 700.0, 7000.0)
    }

    //Duplicates
    otherDuplicates.merge(chunk, 1000.0, 2_000.0)?.let { merged ->
      assertThat(merged.timeStamps).hasSize(3 + 0)
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0)

      assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(7.0)
      assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(70.0)
      assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(700.0)
      assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1))).isEqualTo(7000.0)
    } ?: fail("Where is the exception?")
  }

  @Test
  fun testMerge() {
    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    val other = historyConfiguration.chunk() {
      addDecimalValues(1004.5, 7.0, 70.0, 700.0, 7000.0)
      addDecimalValues(1005.0, 8.0, 80.0, 800.0, 8000.0)
    }

    assertThat(chunk.bestTimestampIndexFor(1000.0).found).isFalse()
    assertThat(chunk.bestTimestampIndexFor(1000.0).nearIndex).isEqualTo(0)

    assertThat(other.bestTimestampIndexFor(1004.5).found).isTrue()
    assertThat(other.bestTimestampIndexFor(1004.5).index).isEqualTo(0)

    val merged = chunk.merge(other, 0.0, 10_000.0)!!

    assertThat(chunk.timeStamps).isEqualTo(doubleArrayOf(1001.0, 1002.0, 1003.0))
    assertThat(other.timeStamps).isEqualTo(doubleArrayOf(1004.5, 1005.0))
    assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0, 1004.5, 1005.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(0))).isEqualTo(100.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(0))).isEqualTo(1000.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(0))).isEqualTo(100.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(0))).isEqualTo(1000.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(2.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(20.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(200.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1))).isEqualTo(2000.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(2.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(20.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(200.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1))).isEqualTo(2000.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(3.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(2))).isEqualTo(30.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(2))).isEqualTo(300.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(2))).isEqualTo(3000.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(3.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(2))).isEqualTo(30.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(2))).isEqualTo(300.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(2))).isEqualTo(3000.0)

    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(7.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(70.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(0))).isEqualTo(700.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(0))).isEqualTo(7000.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(7.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(3))).isEqualTo(70.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(3))).isEqualTo(700.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(3))).isEqualTo(7000.0)

    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(8.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isEqualTo(80.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(1))).isEqualTo(800.0)
    assertThat(other.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(1))).isEqualTo(8000.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(4))).isEqualTo(8.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(4))).isEqualTo(80.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(4))).isEqualTo(800.0)
    assertThat(merged.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(4))).isEqualTo(8000.0)
  }

  @Test
  fun testMergeNonMatching() {
    try {
      val other = historyConfiguration {
        decimalDataSeries(DataSeriesId(222), TextKey.simple("asdf"))
      }.chunk() {
        addDecimalValues(10_000.0, 123.0)
        addDecimalValues(20_000.0, 123.0)
      }

      chunk.merge(other, 0.0, 10_000.0)
      fail("Where is the exception?")
    } catch (e: Exception) {
    }
  }

  @Test
  fun testAddSignificands() {
    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(chunk.timestampCenter(TimestampIndex(0))).isEqualTo(1001.0)
    assertThat(chunk.getDecimalDataSeriesId(DecimalDataSeriesIndex(0)).value).isEqualTo(10)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0) //1 with 1 decimal
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0) //10 with 2 decimals


    //Now add a new timestamp with new values
    val newChunk = chunk.withAddedValues(1004.0, doubleArrayOf(4.0, 40.0, 400.0, 4000.0), emptyIntArray(), emptyIntArray(), emptyIntArray(), emptySet())

    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(newChunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(newChunk.timeStampsCount).isEqualTo(4)

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(4.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(3))).isEqualTo(40.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(3))).isEqualTo(400.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(3))).isEqualTo(4000.0)
  }

  @Test
  fun testAddValues() {
    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(chunk.timestampCenter(TimestampIndex(0))).isEqualTo(1001.0)
    assertThat(chunk.getDecimalDataSeriesId(DecimalDataSeriesIndex(0)).value).isEqualTo(10)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)

    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)
    assertThat(chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isEqualTo(10.0)


    //Now add a new timestamp with new values
    val newChunk = chunk.withAddedValues(1004.0, doubleArrayOf(4.0, 40.0, 400.0, 4000.0), emptyIntArray(), emptyIntArray(), emptyIntArray(), emptySet())

    assertThat(chunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(newChunk.decimalDataSeriesCount).isEqualTo(4)
    assertThat(newChunk.timeStampsCount).isEqualTo(4)

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(1.0)

    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(4.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(3))).isEqualTo(40.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(2), TimestampIndex(3))).isEqualTo(400.0)
    assertThat(newChunk.getDecimalValue(DecimalDataSeriesIndex(3), TimestampIndex(3))).isEqualTo(4000.0)
  }

  @Test
  fun testPending() {
    val newChunk = chunk.withAddedValues(1004.0, DoubleArray(4) { HistoryChunk.Pending }, emptyIntArray(), emptyIntArray(), emptyIntArray(), emptySet())
    assertThat(newChunk.isPending(TimestampIndex(0))).isFalse()
    assertThat(newChunk.isPending(TimestampIndex(1))).isFalse()
    assertThat(newChunk.isPending(TimestampIndex(2))).isFalse()
    assertThat(newChunk.isPending(TimestampIndex(3))).isTrue()
    assertFails("timestamp index out of bounds") { newChunk.isPending(TimestampIndex(4)) }
  }

  @Test
  fun testMaxHistoryAware() {
    assertThat(maxHistoryAware(0.0, 1.0)).isEqualTo(1.0)
    assertThat(maxHistoryAware(20.0, 1.0)).isEqualTo(20.0)

    assertThat(maxHistoryAware(HistoryChunk.Pending, HistoryChunk.Pending)).isEqualTo(HistoryChunk.Pending)
    assertThat(maxHistoryAware(HistoryChunk.NoValue, HistoryChunk.NoValue)).isEqualTo(HistoryChunk.NoValue)

    assertThat(maxHistoryAware(HistoryChunk.NoValue, HistoryChunk.Pending)).isEqualTo(HistoryChunk.NoValue)
    assertThat(maxHistoryAware(HistoryChunk.Pending, HistoryChunk.NoValue)).isEqualTo(HistoryChunk.NoValue)

    assertThat(maxHistoryAware(HistoryChunk.NoValue, 10.0)).isEqualTo(10.0)
    assertThat(maxHistoryAware(10.0, HistoryChunk.NoValue)).isEqualTo(10.0)

    assertThat(maxHistoryAware(HistoryChunk.Pending, 10.0)).isEqualTo(10.0)
    assertThat(maxHistoryAware(10.0, HistoryChunk.Pending)).isEqualTo(10.0)
  }

  @Test
  fun testMinnHistoryAware() {
    assertThat(minHistoryAware(0.0, 1.0)).isEqualTo(0.0)
    assertThat(minHistoryAware(20.0, 1.0)).isEqualTo(1.0)

    assertThat(minHistoryAware(HistoryChunk.Pending, HistoryChunk.Pending)).isEqualTo(HistoryChunk.Pending)
    assertThat(minHistoryAware(HistoryChunk.NoValue, HistoryChunk.NoValue)).isEqualTo(HistoryChunk.NoValue)

    assertThat(minHistoryAware(HistoryChunk.NoValue, HistoryChunk.Pending)).isEqualTo(HistoryChunk.NoValue)
    assertThat(minHistoryAware(HistoryChunk.Pending, HistoryChunk.NoValue)).isEqualTo(HistoryChunk.NoValue)

    assertThat(minHistoryAware(HistoryChunk.NoValue, 10.0)).isEqualTo(10.0)
    assertThat(minHistoryAware(10.0, HistoryChunk.NoValue)).isEqualTo(10.0)

    assertThat(minHistoryAware(HistoryChunk.Pending, 10.0)).isEqualTo(10.0)
    assertThat(minHistoryAware(10.0, HistoryChunk.Pending)).isEqualTo(10.0)
  }
}
