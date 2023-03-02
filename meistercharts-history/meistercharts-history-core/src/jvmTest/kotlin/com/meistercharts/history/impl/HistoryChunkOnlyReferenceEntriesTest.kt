package com.meistercharts.history.impl

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.DataSeriesId
import com.meistercharts.history.HistoryConfiguration
import com.meistercharts.history.ReferenceEntriesDataMap
import com.meistercharts.history.ReferenceEntryDataSeriesIndex
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.historyConfiguration
import com.meistercharts.history.impl.HistoryChunk.Companion.maxHistoryAware
import com.meistercharts.history.impl.HistoryChunk.Companion.minHistoryAware
import com.meistercharts.history.isEqualToReferenceEntryId
import it.neckar.open.collections.emptyDoubleArray
import it.neckar.open.collections.emptyIntArray
import it.neckar.open.serialization.roundTrip
import it.neckar.open.formatting.formatUtc
import it.neckar.open.i18n.TextKey
import it.neckar.open.test.utils.isEqualComparingLinesTrim
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFails

/**
 */
class HistoryChunkOnlyReferenceEntriesTest {
  lateinit var chunk: HistoryChunk

  lateinit var historyConfiguration: HistoryConfiguration

  @BeforeEach
  internal fun setUp() {
    historyConfiguration = historyConfiguration {
      referenceEntryDataSeries(DataSeriesId(10), TextKey("temp", "Temperature"), ReferenceEntriesDataMap.generated)
      referenceEntryDataSeries(DataSeriesId(11), TextKey("height", "Height"), ReferenceEntriesDataMap.generated)
      referenceEntryDataSeries(DataSeriesId(12), TextKey("temp2", "Temperature 2"), ReferenceEntriesDataMap.generated)
      referenceEntryDataSeries(DataSeriesId(13), TextKey("temp3", "Temperature 3"), ReferenceEntriesDataMap.generated)
    }

    assertThat(historyConfiguration.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(historyConfiguration.decimalDataSeriesCount).isEqualTo(0)
    assertThat(historyConfiguration.enumDataSeriesCount).isEqualTo(0)

    chunk = historyConfiguration.chunk() {
      addReferenceEntryValues(1001.0, 1, 10, 100, 1000)
      addReferenceEntryValues(1002.0, 2, 20, 200, 2000)
      addReferenceEntryValues(1003.0, 3, 30, 300, 3000)
    }

    assertThat(chunk).isNotNull()
  }

  @Test
  fun testSerialization() {
    roundTrip(chunk) {
      //language=JSON
      """
        {
          "configuration" : {
            "decimalConfiguration" : {
              "dataSeriesIds" : [ ],
              "displayNames" : [ ],
              "units" : [ ]
            },
            "enumConfiguration" : {
              "dataSeriesIds" : [ ],
              "displayNames" : [ ],
              "enums" : [ ]
            },
            "referenceEntryConfiguration" : {
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
              } ]
            }
          },
          "timeStamps" : [ 1001.0, 1002.0, 1003.0 ],
          "values" : {
            "decimalHistoryValues" : {
              "values" : "AAAAAw==",
              "minValues" : null,
              "maxValues" : null
            },
            "enumHistoryValues" : {
              "values" : "AAAAAw==",
              "mostOfTheTimeValues" : null
            },
            "referenceEntryHistoryValues" : {
              "values" : "AAQAAwAAAAEAAAAKAAAAZAAAA+gAAAACAAAAFAAAAMgAAAfQAAAAAwAAAB4AAAEsAAALuA==",
              "differentIdsCount" : null,
              "dataMaps" : [ {
                  "type" : "Generated"
                }, {
                  "type" : "Generated"
                }, {
                  "type" : "Generated"
                }, {
                  "type" : "Generated"
                } ]
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

    assertThat(chunk.dump()).isEqualComparingLinesTrim(
      """
        Start: 1970-01-01T00:00:01.001
        End:   1970-01-01T00:00:01.003
        Series counts:
          Decimals: 0
          Enums:    0
          RefId:    4
        RecordingType:    Measured
        ---------------------------------------
        Indices:                     |  |           0           1           2           3
        IDs:                         |  |          10          11          12          13

           0 1970-01-01T00:00:01.001 |  |     1   (1)    10   (1)   100   (1)  1000   (1)
           1 1970-01-01T00:00:01.002 |  |     2   (1)    20   (1)   200   (1)  2000   (1)
           2 1970-01-01T00:00:01.003 |  |     3   (1)    30   (1)   300   (1)  3000   (1)
    """.trimIndent()
    )
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
      assertThat(it.decimalDataSeriesCount).isEqualTo(0)
      assertThat(it.enumDataSeriesCount).isEqualTo(0)
      assertThat(it.referenceEntryDataSeriesCount).isEqualTo(4)
    }
  }

  @Test
  fun testSubRange() {
    assertThat(chunk.getDecimalDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(-1) //no decimal
    assertThat(chunk.getEnumDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(-1) //no enum
    assertThat(chunk.getReferenceEntryDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(0)

    assertThat(chunk.timeStampsCount).isEqualTo(3)


    assertThat(chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
    assertThat(chunk.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")

    assertThat(chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
    assertThat(chunk.timestampCenter(TimestampIndex(2)).formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    assertThat(chunk.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")

    chunk.range(0.0, chunk.timestampCenter(TimestampIndex(2)))!!.let {
      //contains everything
      assertThat(it.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
    }

    chunk.range(chunk.timestampCenter(TimestampIndex(0)), 999999999999.0)!!.let {
      //contains everything
      assertThat(it.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    }

    chunk.range(0.0, chunk.timestampCenter(TimestampIndex(1)))!!.let {
      //contains *not*
      assertThat(it.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
      assertThat(it.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.001")
    }

    chunk.range(chunk.timestampCenter(TimestampIndex(1)), 999999999999999.0)!!.let {
      //contains *not*
      assertThat(it.start.formatUtc()).isEqualTo("1970-01-01T00:00:01.002")
      assertThat(it.end.formatUtc()).isEqualTo("1970-01-01T00:00:01.003")
    }
  }

  @Test
  fun testSize() {
    chunk.getReferenceEntryIds(TimestampIndex(0)).let {
      assertThat(it).hasSize(historyConfiguration.referenceEntryConfiguration.dataSeriesIds.size)
    }
  }

  @Test
  fun testValues() {
    assertThat(chunk.getReferenceEntryDataSeriesIndex(DataSeriesId(10)).value).isEqualTo(0)
    assertThat(chunk.getReferenceEntryDataSeriesIndex(DataSeriesId(11)).value).isEqualTo(1)
    assertThat(chunk.getReferenceEntryDataSeriesIndex(DataSeriesId(12)).value).isEqualTo(2)
    assertThat(chunk.getReferenceEntryDataSeriesIndex(DataSeriesId(13)).value).isEqualTo(3)

    val a = chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(2)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(20)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToReferenceEntryId(100)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(200)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(0))).isEqualToReferenceEntryId(1000)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(2000)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(2)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(20)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToReferenceEntryId(100)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(200)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(0))).isEqualToReferenceEntryId(1000)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(2000)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(2)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(20)
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
    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    val other = historyConfiguration.chunk() {
      addReferenceEntryValues(2000.0, 7, 70, 700, 7000)
      addReferenceEntryValues(5000.0, 8, 80, 800, 8000)
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
    val otherDuplicates = historyConfiguration.chunk {
      addReferenceEntryValues(1002.0, 7, 70, 700, 7000)
    }

    //Duplicates
    otherDuplicates.merge(chunk, 1000.0, 2_000.0)?.let { merged ->
      assertThat(merged.timeStamps).hasSize(3 + 0)
      assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0)

      assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(7)
      assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(7)
      assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(70)
      assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(700)
      assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(7000)
    } ?: fail("Where is the exception?")
  }

  @Test
  fun testMerge() {
    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    val other = historyConfiguration.chunk() {
      addReferenceEntryValues(1004.5, 7, 70, 700, 7000)
      addReferenceEntryValues(1005.0, 8, 80, 800, 8000)
    }

    assertThat(chunk.bestTimestampIndexFor(1000.0).found).isFalse()
    assertThat(chunk.bestTimestampIndexFor(1000.0).nearIndex).isEqualTo(0)

    assertThat(other.bestTimestampIndexFor(1004.5).found).isTrue()
    assertThat(other.bestTimestampIndexFor(1004.5).index).isEqualTo(0)

    val merged = chunk.merge(other, 0.0, 10_000.0)!!

    assertThat(chunk.timeStamps).isEqualTo(doubleArrayOf(1001.0, 1002.0, 1003.0))
    assertThat(other.timeStamps).isEqualTo(doubleArrayOf(1004.5, 1005.0))
    assertThat(merged.timeStamps).containsExactly(1001.0, 1002.0, 1003.0, 1004.5, 1005.0)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToReferenceEntryId(100)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(0))).isEqualToReferenceEntryId(1000)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToReferenceEntryId(100)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(0))).isEqualToReferenceEntryId(1000)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(2)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(20)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(200)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(2000)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(2)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(20)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(200)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(2000)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(2))).isEqualToReferenceEntryId(3)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(2))).isEqualToReferenceEntryId(30)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(2))).isEqualToReferenceEntryId(300)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(2))).isEqualToReferenceEntryId(3000)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(2))).isEqualToReferenceEntryId(3)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(2))).isEqualToReferenceEntryId(30)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(2))).isEqualToReferenceEntryId(300)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(2))).isEqualToReferenceEntryId(3000)

    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(7)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(70)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(0))).isEqualToReferenceEntryId(700)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(0))).isEqualToReferenceEntryId(7000)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(3))).isEqualToReferenceEntryId(7)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(3))).isEqualToReferenceEntryId(70)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(3))).isEqualToReferenceEntryId(700)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(3))).isEqualToReferenceEntryId(7000)

    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(1))).isEqualToReferenceEntryId(8)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(1))).isEqualToReferenceEntryId(80)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(1))).isEqualToReferenceEntryId(800)
    assertThat(other.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(1))).isEqualToReferenceEntryId(8000)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(4))).isEqualToReferenceEntryId(8)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(4))).isEqualToReferenceEntryId(80)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(4))).isEqualToReferenceEntryId(800)
    assertThat(merged.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(4))).isEqualToReferenceEntryId(8000)
  }

  @Test
  fun testMergeNonMatching() {
    assertThat {
      val other = historyConfiguration {
        decimalDataSeries(DataSeriesId(222), TextKey.simple("asdf"))
      }.chunk {
        addReferenceEntryValues(10_000.0, 123)
        addReferenceEntryValues(20_000.0, 123)
      }

      chunk.merge(other, 0.0, 10_000.0)
    }.isFailure()
  }

  @Test
  fun testAddValues2() {
    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(chunk.timestampCenter(TimestampIndex(0))).isEqualTo(1001.0)
    assertThat(chunk.getReferenceEntryDataSeriesId(ReferenceEntryDataSeriesIndex(0)).value).isEqualTo(10)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)


    //Now add a new timestamp with new values
    val newChunk = chunk.withAddedValues(1004.0, emptyDoubleArray(), emptyIntArray(), intArrayOf(4, 40, 400, 4000))

    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(newChunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(newChunk.timeStampsCount).isEqualTo(4)

    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)

    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(3))).isEqualToReferenceEntryId(4)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(3))).isEqualToReferenceEntryId(40)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(3))).isEqualToReferenceEntryId(400)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(3))).isEqualToReferenceEntryId(4000)
  }

  @Test
  fun testAddValues() {
    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(chunk.timestampCenter(TimestampIndex(0))).isEqualTo(1001.0)
    assertThat(chunk.getReferenceEntryDataSeriesId(ReferenceEntryDataSeriesIndex(0)).value).isEqualTo(10)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)

    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)
    assertThat(chunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(0))).isEqualToReferenceEntryId(10)


    //Now add a new timestamp with new values
    val newChunk = chunk.withAddedValues(1004.0, emptyDoubleArray(), emptyIntArray(), intArrayOf(4, 40, 400, 4000))

    assertThat(chunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(chunk.timeStampsCount).isEqualTo(3)

    assertThat(newChunk.referenceEntryDataSeriesCount).isEqualTo(4)
    assertThat(newChunk.timeStampsCount).isEqualTo(4)

    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(0))).isEqualToReferenceEntryId(1)

    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(0), TimestampIndex(3))).isEqualToReferenceEntryId(4)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(1), TimestampIndex(3))).isEqualToReferenceEntryId(40)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(2), TimestampIndex(3))).isEqualToReferenceEntryId(400)
    assertThat(newChunk.getReferenceEntryId(ReferenceEntryDataSeriesIndex(3), TimestampIndex(3))).isEqualToReferenceEntryId(4000)
  }

  @Test
  fun testPending() {
    val newChunk = chunk.withAddedValues(1004.0, emptyDoubleArray(), emptyIntArray(), IntArray(4) { ReferenceEntryId.PendingAsInt })
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
