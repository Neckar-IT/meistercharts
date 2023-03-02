package com.meistercharts.history.impl.io

import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.TimestampIndex
import com.meistercharts.history.impl.HistoryValues
import com.meistercharts.history.impl.RecordingType
import com.meistercharts.history.impl.historyValues
import it.neckar.open.collections.DoubleArray2
import it.neckar.open.collections.IntArray2
import it.neckar.open.collections.invokeCols
import it.neckar.open.serialization.roundTrip
import com.meistercharts.history.ReferenceEntriesDataMap
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 */
class HistoryValuesSerializationTest {
  @Test
  fun testMeasured() {
    val historyValues = historyValues(2, 1, 1, 3, RecordingType.Measured) {
      setAllValuesForTimestamp(timestampIndex = TimestampIndex(0), decimalValues = doubleArrayOf(1.0, 2.0), minValues = null, maxValues = null, enumValues = intArrayOf(7), enumOrdinalsMostTime = null, referenceEntryIds = intArrayOf(6))
      setAllValuesForTimestamp(timestampIndex = TimestampIndex(1), decimalValues = doubleArrayOf(1.1, 2.1), minValues = null, maxValues = null, enumValues = intArrayOf(8), enumOrdinalsMostTime = null, referenceEntryIds = intArrayOf(7))
      setAllValuesForTimestamp(timestampIndex = TimestampIndex(2), decimalValues = doubleArrayOf(1.2, 2.2), minValues = null, maxValues = null, enumValues = intArrayOf(9), enumOrdinalsMostTime = null, referenceEntryIds = intArrayOf(8))
    }

    roundTrip(historyValues) {
      //language=JSON
      """
        {
          "decimalHistoryValues" : {
            "values" : "AAIAAz/wAAAAAAAAQAAAAAAAAAA/8ZmZmZmZmkAAzMzMzMzNP/MzMzMzMzNAAZmZmZmZmg==",
            "minValues" : null,
            "maxValues" : null
          },
          "enumHistoryValues" : {
            "values" : "AAEAAwAAAAcAAAAIAAAACQ==",
            "mostOfTheTimeValues" : null
          },
          "referenceEntryHistoryValues" : {
            "values" : "AAEAAwAAAAYAAAAHAAAACA==",
                "differentIdsCount" : null,
                "dataMaps" : [ {
                  "type" : "Generated"
                } ]
          }
        }
      """.trimIndent()
    }
  }

  @Test
  fun testCalculated() {
    val historyValues = historyValues(2, 1, 1, 3, RecordingType.Calculated) {
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(0),
        decimalValues = doubleArrayOf(1.0, 2.0),
        minValues = doubleArrayOf(0.5, 1.5),
        maxValues = doubleArrayOf(5.5, 6.5),
        enumValues = intArrayOf(7),
        enumOrdinalsMostTime = intArrayOf(11),
        referenceEntryIds = intArrayOf(6),
        referenceEntryDifferentIdsCount = intArrayOf(1)
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(1),
        decimalValues = doubleArrayOf(1.1, 2.1),
        minValues = doubleArrayOf(0.6, 1.6),
        maxValues = doubleArrayOf(5.6, 6.6),
        enumValues = intArrayOf(8),
        enumOrdinalsMostTime = intArrayOf(12),
        referenceEntryIds = intArrayOf(7),
        referenceEntryDifferentIdsCount = intArrayOf(1)
      )
      setAllValuesForTimestamp(
        timestampIndex = TimestampIndex(2),
        decimalValues = doubleArrayOf(1.2, 2.2),
        minValues = doubleArrayOf(0.7, 1.7),
        maxValues = doubleArrayOf(5.7, 6.7),
        enumValues = intArrayOf(9),
        enumOrdinalsMostTime = intArrayOf(13),
        referenceEntryIds = intArrayOf(8),
        referenceEntryDifferentIdsCount = intArrayOf(1)
      )
    }

    roundTrip(historyValues) {
      //language=JSON
      """
        {
          "decimalHistoryValues" : {
            "values" : "AAIAAz/wAAAAAAAAQAAAAAAAAAA/8ZmZmZmZmkAAzMzMzMzNP/MzMzMzMzNAAZmZmZmZmg==",
            "minValues" : "AAIAAz/gAAAAAAAAP/gAAAAAAAA/4zMzMzMzMz/5mZmZmZmaP+ZmZmZmZmY/+zMzMzMzMw==",
            "maxValues" : "AAIAA0AWAAAAAAAAQBoAAAAAAABAFmZmZmZmZkAaZmZmZmZmQBbMzMzMzM1AGszMzMzMzQ=="
          },
          "enumHistoryValues" : {
            "values" : "AAEAAwAAAAcAAAAIAAAACQ==",
            "mostOfTheTimeValues" : "AAEAAwAAAAsAAAAMAAAADQ=="
          },
          "referenceEntryHistoryValues" : {
            "values" : "AAEAAwAAAAYAAAAHAAAACA==",
                "differentIdsCount" : "AAEAAwAAAAEAAAABAAAAAQ==",
                "dataMaps" : [ {
                  "type" : "Generated"
                } ]
          }
        }
      """.trimIndent()
    }
  }

  @Deprecated("no longer required/used")
  @Disabled
  @Test
  fun testMakeRelative() {
    val absolute = HistoryValues(
      decimalValues = DoubleArray2.invokeCols(
        arrayOf(
          doubleArrayOf(1.0, 2.0, 3.0),
          doubleArrayOf(10.0, 20.0, 30.0),
          doubleArrayOf(100.0, 200.0, 300.0),
          doubleArrayOf(1000.0, 2000.0, 3000.0)
        )
      ),
      enumValues = IntArray2.invokeCols(
        arrayOf(
          intArrayOf(1, 2, 3),
          intArrayOf(10, 20, 30),
          intArrayOf(100, 200, 300),
          intArrayOf(1000, 2000, 3000)
        )
      ),
      referenceEntryIds = IntArray2.invokeCols(
        arrayOf(
          intArrayOf(7, 8, 9),
          intArrayOf(70, 80, 90),
          intArrayOf(700, 800, 900),
          intArrayOf(7000, 8000, 9000)
        )
      ),
      referenceEntriesDataMaps = List(4) { ReferenceEntriesDataMap.generated },
    )

    assertThat(absolute.decimalDataSeriesCount).isEqualTo(4)
    assertThat(absolute.timeStampsCount).isEqualTo(3)

    assertThat(absolute.decimalHistoryValues.values[0, 0]).isEqualTo(1.0)
    assertThat(absolute.decimalHistoryValues.values[0, 1]).isEqualTo(2.0)
    assertThat(absolute.decimalHistoryValues.values[0, 2]).isEqualTo(3.0)

    assertThat(absolute.decimalHistoryValues.values[1, 0]).isEqualTo(10.0)
    assertThat(absolute.decimalHistoryValues.values[1, 1]).isEqualTo(20.0)


    assertThat(absolute.decimalHistoryValues.values.data.size).isEqualTo(12)

    assertThat(absolute.decimalHistoryValues.values.data[0]).isEqualTo(1.0)
    assertThat(absolute.decimalHistoryValues.values.data[1]).isEqualTo(10.0)
    assertThat(absolute.decimalHistoryValues.values.data[2]).isEqualTo(100.0)
    assertThat(absolute.decimalHistoryValues.values.data[3]).isEqualTo(1000.0)
    assertThat(absolute.decimalHistoryValues.values.data[4]).isEqualTo(2.0)
    assertThat(absolute.decimalHistoryValues.values.data[5]).isEqualTo(20.0)
    assertThat(absolute.decimalHistoryValues.values.data[6]).isEqualTo(200.0)
    assertThat(absolute.decimalHistoryValues.values.data[7]).isEqualTo(2000.0)
    assertThat(absolute.decimalHistoryValues.values.data[8]).isEqualTo(3.0)
    assertThat(absolute.decimalHistoryValues.values.data[9]).isEqualTo(30.0)
    assertThat(absolute.decimalHistoryValues.values.data[10]).isEqualTo(300.0)
    assertThat(absolute.decimalHistoryValues.values.data[11]).isEqualTo(3000.0)


    //Make relative
    val relative = absolute.makeRelative()
    assertThat(relative.decimalsDataSeriesCount).isEqualTo(4)
    assertThat(relative.timeStampsCount).isEqualTo(3)

    assertThat(relative.decimalValues.data[0]).isEqualTo(1.0) //First value
    assertThat(relative.decimalValues.data[1]).isEqualTo(10.0) //First value
    assertThat(relative.decimalValues.data[2]).isEqualTo(100.0) //First value
    assertThat(relative.decimalValues.data[3]).isEqualTo(1000.0) //First value
    assertThat(relative.decimalValues.data[4]).isEqualTo(1.0) //relative!
    assertThat(relative.decimalValues.data[5]).isEqualTo(10.0) //relative!
    assertThat(relative.decimalValues.data[6]).isEqualTo(100.0) //relative!
    assertThat(relative.decimalValues.data[7]).isEqualTo(1000.0) //relative!
    assertThat(relative.decimalValues.data[8]).isEqualTo(1.0) //relative!
    assertThat(relative.decimalValues.data[9]).isEqualTo(10.0) //relative!
    assertThat(relative.decimalValues.data[10]).isEqualTo(100.0) //relative!
    assertThat(relative.decimalValues.data[11]).isEqualTo(1000.0) //relative!


    assertThat(relative.decimalValues[0, 0]).isEqualTo(1.0) //first value
    assertThat(relative.decimalValues[0, 1]).isEqualTo(1.0) //relative
    assertThat(relative.decimalValues[0, 2]).isEqualTo(1.0) //relative

    assertThat(relative.decimalValues[1, 0]).isEqualTo(10.0) //first value
    assertThat(relative.decimalValues[1, 1]).isEqualTo(10.0) //relative
    assertThat(relative.decimalValues[1, 2]).isEqualTo(10.0) //relative

    assertThat(relative.decimalValues[2, 0]).isEqualTo(100.0) //first value
    assertThat(relative.decimalValues[2, 1]).isEqualTo(100.0) //relative
    assertThat(relative.decimalValues[2, 2]).isEqualTo(100.0) //relative

    assertThat(relative.decimalValues[3, 0]).isEqualTo(1000.0) //first value
    assertThat(relative.decimalValues[3, 1]).isEqualTo(1000.0) //relative
    assertThat(relative.decimalValues[3, 2]).isEqualTo(1000.0) //relative


    //Make absolute again
    val absoluteBack = relative.makeAbsolute()

    assertThat(absoluteBack.decimalHistoryValues.values.data.contentEquals(absolute.decimalHistoryValues.values.data)).isTrue()

    assertThat(absoluteBack.decimalHistoryValues.values).isEqualTo(absolute.decimalHistoryValues.values)

    assertThat(absoluteBack)
      .describedAs(absoluteBack.decimalValuesAsMatrixString())
      .isEqualTo(absolute)
      .describedAs(absolute.decimalValuesAsMatrixString())

    assertThat(absoluteBack.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(0))).isEqualTo(1.0)
    assertThat(absoluteBack.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(1))).isEqualTo(2.0)
    assertThat(absoluteBack.getDecimalValue(DecimalDataSeriesIndex.zero, TimestampIndex(2))).isEqualTo(3.0)
  }

  //@Test
  //internal fun testSerialize() {
  //  val values = HistoryValues(
  //    arrayOf(
  //      intArrayOf(1, 2, 3),
  //      intArrayOf(10, 20, 30),
  //      intArrayOf(100, 200, 300),
  //      intArrayOf(1000, 2000, 3000)
  //    )
  //  )
  //
  //  val json = Json.stringify(HistoryValues.serializer(), values)
  //  println("Json: $json")
  //  val parsed = Json.parse(HistoryValues.serializer(), json)
  //
  //  assertThat(parsed).isEqualTo(values)
  //}
}
