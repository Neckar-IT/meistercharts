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
package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import assertk.assertions.support.*
import com.meistercharts.history.DecimalDataSeriesIndex
import com.meistercharts.history.EnumDataSeriesIndex
import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.HistoryEnumSet
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.TimestampIndex
import it.neckar.open.formatting.formatUtc
import it.neckar.open.test.utils.isCloseTo
import it.neckar.open.test.utils.isNaN
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 *
 */
class DownSamplingTest {
  @Test
  fun testBitSet() {
    println("Result: ${4 or 5}")
  }

  @Test
  @Disabled
  fun printDownSamplingSizes() {
    HistoryBucketRange.values().forEach {
      println("${it.name}\t-\t${it.samplingPeriod.name}")

      if (it != HistoryBucketRange.HundredMillis) {
        println("\t Down sampling size: ${it.downSamplingFactor()}")
      }
    }
  }

  @Test
  fun testCalculateTimeStamps() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(1000000000.0, HistoryBucketRange.FiveSeconds)

    val timeStamps = descriptor.calculateTimeStamps()
    assertThat(timeStamps).hasSize(descriptor.bucketRange.entriesCount)
    assertThat(timeStamps.first()).isEqualTo(descriptor.start + descriptor.bucketRange.distance / 2.0)
    assertThat(timeStamps.last()).isEqualTo(descriptor.end - descriptor.bucketRange.distance / 2.0)
  }

  //@Test
  //fun testDownSamplingSuperSimple() {
  //  assertThat(nowForTests.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")
  //
  //  val descriptor0 = HistoryBucketDescriptor.forTimestamp(nowForTests, HistoryBucketRange.FiveSeconds)
  //  assertThat(descriptor0.start.formatUtc()).isEqualTo("2020-05-21T15:00:40.000")
  //  assertThat(descriptor0.end.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")
  //
  //  val descriptor1 = descriptor0.next()
  //  assertThat(descriptor1.start.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")
  //  assertThat(descriptor1.end.formatUtc()).isEqualTo("2020-05-21T15:00:50.000")
  //
  //
  //  historyChunk()
  //
  //  val historyValues = historyValues(0, 1, 3, RecordingType.Measured) {
  //
  //  }
  //
  //  val bucket0 = HistoryBucket(
  //    descriptor0,
  //    HistoryChunk()
  //
  //    )
  //  val parentDescriptor = descriptor0.parent() ?: throw IllegalStateException()
  //  val downSampled = parentDescriptor.calculateDownSampled(listOf(bucket0))
  //
  //  println(downSampled.chunk.enumValuesAsMatrixString())
  //
  //}

  @Test
  fun testDownSamplingEnum() {
    assertThat(nowForTests.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")

    val descriptor0 = HistoryBucketDescriptor.forTimestamp(nowForTests, HistoryBucketRange.FiveSeconds)
    assertThat(descriptor0.start.formatUtc()).isEqualTo("2020-05-21T15:00:40.000")
    assertThat(descriptor0.end.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")

    val descriptor1 = descriptor0.next()
    assertThat(descriptor1.start.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")
    assertThat(descriptor1.end.formatUtc()).isEqualTo("2020-05-21T15:00:50.000")

    val bucket0 = HistoryBucket(descriptor0, createDemoChunkOnlyEnums(descriptor0) { dataStructureIndex, timestampIndex ->
      HistoryEnumSet(demoEnumBitset(dataStructureIndex, timestampIndex))
    })
    val bucket1 = HistoryBucket(descriptor1, createDemoChunkOnlyEnums(descriptor1) { dataStructureIndex, timestampIndex ->
      HistoryEnumSet(demoEnumBitset(dataStructureIndex, timestampIndex))
    })

    val parentDescriptor = descriptor0.parent() ?: throw IllegalStateException()

    assertThat(parentDescriptor.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)
    assertThat(parentDescriptor.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(parentDescriptor.end.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(parentDescriptor.center.formatUtc()).isEqualTo("2020-05-21T15:00:30.000")

    assertThat(parentDescriptor.bucketRange.downSamplingFactor()).isEqualTo(10)


    parentDescriptor.children().let { children ->
      assertThat(children.size).isEqualTo(12)
      assertThat(children.contains(descriptor0)).isTrue()
      assertThat(children.contains(descriptor1)).isTrue()

      assertThat(children.indexOf(descriptor0)).isEqualTo(8)
      assertThat(children.indexOf(descriptor1)).isEqualTo(9)

      assertThat(children[0].bucketRange).isEqualTo(HistoryBucketRange.FiveSeconds)
      assertThat(children[0].start.formatUtc()).isEqualTo(parentDescriptor.start.formatUtc())
      assertThat(children.last().end.formatUtc()).isEqualTo(parentDescriptor.end.formatUtc())
    }

    val downSampled = parentDescriptor.calculateDownSampled(listOf(bucket0, bucket1))

    if (false) {
      println("bucket0-------------------")
      println(bucket0.chunk.dump())
      println("/bucket0-------------------")

      println("downSampled-------------------")
      println(downSampled.chunk.dump())
      println("/downSampled-------------------")
    }

    //we expect 600 entries in the down sampled chunk
    assertThat(downSampled.chunk.timeStampsCount).all {
      isEqualTo(parentDescriptor.bucketRange.entriesCount)
      isEqualTo(600)
    }

    assertThat(downSampled.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)

    assertThat(downSampled.start.formatUtc()).isEqualTo(parentDescriptor.start.formatUtc())
    assertThat(downSampled.end.formatUtc()).isEqualTo(parentDescriptor.end.formatUtc())

    //Verify the values
    assertThat(HistoryBucketRange.OneMinute.distance).isEqualTo(100.0) //one data point every 100 ms
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:00.050") //start + 50ms (100 ms/2)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:00.150") //start + 50ms + 100ms
    assertThat(downSampled.chunk.lastTimeStamp().formatUtc()).isEqualTo("2020-05-21T15:00:59.950") //end - 50ms (100 ms/2)

    assertThat(bucket0.bucketRange).isEqualTo(HistoryBucketRange.FiveSeconds)
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:40.000") //start at full minute
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:40.010") //every 10 ms
    assertThat(bucket1.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:45.000") //start at full minute
    assertThat(bucket1.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:45.010") //every 10 ms one value


    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(0))).isPending()
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1))).isPending()
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(2))).isPending()
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(3))).isPending()

    //only the values *in the middle* are relevant, because the original chunk is in the middle
    bucket0.chunk.timestampCenter(TimestampIndex(0)).let { timestamp ->
      assertThat(timestamp.formatUtc()).isEqualTo("2020-05-21T15:00:40.000") //the timestamp of the original bucket

      downSampled.chunk.bestTimestampIndexFor(timestamp).let { index ->
        assertThat(index.found).isFalse()
        assertThat(index.nearIndex).isEqualTo(400) //0:00 -> 1:00

        assertThat(downSampled.chunk.timestampCenter(TimestampIndex(index.nearIndex)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050") //the timestamp of the *center* of the slot
        assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(index.nearIndex))).isEqualTo(HistoryEnumSet(0b1111))

        //compare with original values
        assertThat(bucket0.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(HistoryEnumSet(0b1))
        assertThat(bucket0.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(HistoryEnumSet(0b10))

        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:40.000")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:40.010")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(5)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(9)).formatUtc()).isEqualTo("2020-05-21T15:00:40.090")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(10)).formatUtc()).isEqualTo("2020-05-21T15:00:40.100")
      }
    }

    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(0))).isPending()
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(1))).isPending()

    //Last entry that has no value (before bucket0)
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(399))).isPending()
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(399)).formatUtc()).isEqualTo("2020-05-21T15:00:39.950")


    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(400))).isEqualTo(HistoryEnumSet(0b1111))
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(400)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050")

    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(401))).isEqualTo(HistoryEnumSet(0b11111))
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(401)).formatUtc()).isEqualTo("2020-05-21T15:00:40.150")

    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(448))).isEqualTo(HistoryEnumSet(0b111101111))
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(448)).formatUtc()).isEqualTo("2020-05-21T15:00:44.850")

    //last one containing only data from bucket0
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(449))).isEqualTo(HistoryEnumSet(0b111111111))
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(449)).formatUtc()).isEqualTo("2020-05-21T15:00:44.950")


    //first one from bucket 1
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(0), TimestampIndex(450))).isEqualTo(HistoryEnumSet(0b1111))
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(450)).formatUtc()).isEqualTo("2020-05-21T15:00:45.050")


    //Verify the values for the other data series
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(0))).isPending()
    assertThat(downSampled.chunk.getEnumValue(EnumDataSeriesIndex(1), TimestampIndex(1))).isPending()
  }

  /**
   * Creates a enum bit set
   */
  private fun demoEnumBitset(dataStructureIndex: EnumDataSeriesIndex, timestampIndex: TimestampIndex): Int {
    val sum = dataStructureIndex.value + timestampIndex.value
    return sum % (HistoryEnumSet.Max.bitset - 1) + 1
  }

  @Test
  fun testDownSamplingDecimal() {
    assertThat(nowForTests.formatUtc()).isEqualTo("2020-05-21T15:00:41.500")

    val descriptor0 = HistoryBucketDescriptor.forTimestamp(nowForTests, HistoryBucketRange.FiveSeconds)
    assertThat(descriptor0.start.formatUtc()).isEqualTo("2020-05-21T15:00:40.000")
    assertThat(descriptor0.end.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")

    val descriptor1 = descriptor0.next()
    assertThat(descriptor1.start.formatUtc()).isEqualTo("2020-05-21T15:00:45.000")
    assertThat(descriptor1.end.formatUtc()).isEqualTo("2020-05-21T15:00:50.000")

    val bucket0 = HistoryBucket(descriptor0, createDemoChunkOnlyDecimals(descriptor0) { dataSeriesIndex, timestampIndex ->
      dataSeriesIndex.value.toDouble() + timestampIndex.value
    })
    val bucket1 = HistoryBucket(descriptor1, createDemoChunkOnlyDecimals(descriptor1) { dataSeriesIndex, timestampIndex ->
      dataSeriesIndex.value.toDouble() + timestampIndex.value
    })


    val parentDescriptor = descriptor0.parent() ?: throw IllegalStateException()

    assertThat(parentDescriptor.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)
    assertThat(parentDescriptor.start.formatUtc()).isEqualTo("2020-05-21T15:00:00.000")
    assertThat(parentDescriptor.end.formatUtc()).isEqualTo("2020-05-21T15:01:00.000")
    assertThat(parentDescriptor.center.formatUtc()).isEqualTo("2020-05-21T15:00:30.000")

    assertThat(parentDescriptor.bucketRange.downSamplingFactor()).isEqualTo(10)

    parentDescriptor.children().let { children ->
      assertThat(children.size).isEqualTo(12)
      assertThat(children.contains(descriptor0)).isTrue()
      assertThat(children.contains(descriptor1)).isTrue()

      assertThat(children.indexOf(descriptor0)).isEqualTo(8)
      assertThat(children.indexOf(descriptor1)).isEqualTo(9)

      assertThat(children[0].bucketRange).isEqualTo(HistoryBucketRange.FiveSeconds)
      assertThat(children[0].start.formatUtc()).isEqualTo(parentDescriptor.start.formatUtc())
      assertThat(children.last().end.formatUtc()).isEqualTo(parentDescriptor.end.formatUtc())
    }

    val downSampled = parentDescriptor.calculateDownSampled(listOf(bucket0, bucket1))

    //we expect 600 entries in the down sampled chunk
    assertThat(downSampled.chunk.timeStampsCount).all {
      isEqualTo(parentDescriptor.bucketRange.entriesCount)
      isEqualTo(600)
    }

    assertThat(downSampled.bucketRange).isEqualTo(HistoryBucketRange.OneMinute)

    assertThat(downSampled.start.formatUtc()).isEqualTo(parentDescriptor.start.formatUtc())
    assertThat(downSampled.end.formatUtc()).isEqualTo(parentDescriptor.end.formatUtc())

    //Verify the values
    assertThat(HistoryBucketRange.OneMinute.distance).isEqualTo(100.0) //one data point every 100 ms
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:00.050") //start + 50ms (100 ms/2)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:00.150") //start + 50ms + 100ms
    assertThat(downSampled.chunk.lastTimeStamp().formatUtc()).isEqualTo("2020-05-21T15:00:59.950") //end - 50ms (100 ms/2)

    assertThat(bucket0.bucketRange).isEqualTo(HistoryBucketRange.FiveSeconds)
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:40.000") //start at full minute
    assertThat(bucket0.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:40.010") //every 10 ms
    assertThat(bucket1.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:45.000") //start at full minute
    assertThat(bucket1.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:45.010") //every 10 ms one value


    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isNaN()
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isNaN()
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isNaN()
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isNaN()


    //only the values *in the middle* are relevant, because the original chunk is in the middle
    bucket0.chunk.timestampCenter(TimestampIndex(0)).let { timestamp ->
      assertThat(timestamp.formatUtc()).isEqualTo("2020-05-21T15:00:40.000") //the timestamp of the original bucket

      downSampled.chunk.bestTimestampIndexFor(timestamp).let { index ->
        assertThat(index.found).isFalse()
        assertThat(index.nearIndex).isEqualTo(400) //0:00 -> 1:00

        assertThat(downSampled.chunk.timestampCenter(TimestampIndex(index.nearIndex)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050") //the timestamp of the *center* of the slot
        assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(index.nearIndex))).isEqualTo(4.5)

        //compare with original values
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isEqualTo(0.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isEqualTo(1.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(2))).isEqualTo(2.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(3))).isEqualTo(3.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(4))).isEqualTo(4.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(5))).isEqualTo(5.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(9))).isEqualTo(9.0)
        assertThat(bucket0.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(10))).isEqualTo(10.0)

        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(0)).formatUtc()).isEqualTo("2020-05-21T15:00:40.000")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(1)).formatUtc()).isEqualTo("2020-05-21T15:00:40.010")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(5)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(9)).formatUtc()).isEqualTo("2020-05-21T15:00:40.090")
        assertThat(bucket0.chunk.timestampCenter(TimestampIndex(10)).formatUtc()).isEqualTo("2020-05-21T15:00:40.100")

      }
    }


    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(0))).isNaN()
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(1))).isNaN()

    //Last entry that has no value (before bucket0)
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(399))).isNaN()
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(399)).formatUtc()).isEqualTo("2020-05-21T15:00:39.950")


    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(400))).isEqualTo(4.5)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(400)).formatUtc()).isEqualTo("2020-05-21T15:00:40.050")

    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(401))).isEqualTo(14.5)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(401)).formatUtc()).isEqualTo("2020-05-21T15:00:40.150")

    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(448))).isEqualTo(484.5)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(448)).formatUtc()).isEqualTo("2020-05-21T15:00:44.850")

    //last one containing only data from bucket0
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(449))).isEqualTo(494.5)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(449)).formatUtc()).isEqualTo("2020-05-21T15:00:44.950")


    //first one from bucket 1
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(0), TimestampIndex(450))).isEqualTo(4.5)
    assertThat(downSampled.chunk.timestampCenter(TimestampIndex(450)).formatUtc()).isEqualTo("2020-05-21T15:00:45.050")


    //Verify the values for the other data series
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(0))).isNaN()
    assertThat(downSampled.chunk.getDecimalValue(DecimalDataSeriesIndex(1), TimestampIndex(1))).isNaN()
  }

  @Test
  fun testCalcAverages() {
    val values = doubleArrayOf(1.0, 3.0, 3.0, 5.0, 5.0, 9.0)
    assertThat(values.calculateMeanValues(2)).isEqualTo(doubleArrayOf(2.0, 4.0, 7.0))
    assertThat(values.calculateMeanValues(3)).isCloseTo(doubleArrayOf(2.3333, 6.33333), 0.001)
  }

  @Test
  fun testMax() {
    val values = intArrayOf(1, 3, 3, 5, 5, 9)
    assertThat(values.calculateMax(2)).isEqualTo(intArrayOf(3, 5, 9))
  }

  @Test
  fun testMin() {
    val values = intArrayOf(1, 3, 3, 5, 5, 9)
    assertThat(values.calculateMin(2)).isEqualTo(intArrayOf(1, 3, 5))
  }

  @Test
  fun testStdDeviation() {
    val values = doubleArrayOf(1.0, 3.0, 3.0, 5.0, 5.0, 9.0)
    val means = values.calculateMeanValues(2)
    assertThat(values.calculateStandardDeviation(2, means)[0]).isCloseTo(1.414, 0.001)
    assertThat(values.calculateStandardDeviation(2, means)[1]).isCloseTo(1.414, 0.001)
    assertThat(values.calculateStandardDeviation(2, means)[2]).isCloseTo(2.828, 0.001)
  }

  @Test
  fun testCombineStdDeviation() {
    val values = doubleArrayOf(1.0, 3.0, 3.0, 5.0, 5.0, 9.0)
    val means = values.calculateMeanValues(2)
    val standardDeviations = values.calculateStandardDeviation(2, means)

    assertThat(standardDeviations).hasSize(3)

    assertThat(standardDeviations.combineStandardDeviations(3)).hasSize(1)
    assertThat(standardDeviations.combineStandardDeviations(3)[0]).isEqualTo(2.0)
  }

  @Test
  fun testIt() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(1000000000.0, HistoryBucketRange.HundredMillis)

    assertThat(descriptor.bucketRange.entriesCount).isEqualTo(100)

    val bucket = HistoryBucket(descriptor, createDemoChunkOnlyDecimals(descriptor) { dataSeriesIndex, timestampIndex ->
      dataSeriesIndex.value.toDouble() + timestampIndex.value
    })

    assertThat(bucket.chunk.isEmpty()).isFalse()
    assertThat(bucket.chunk.decimalDataSeriesCount).isEqualTo(3)
    assertThat(bucket.chunk.enumDataSeriesCount).isEqualTo(0)
    assertThat(bucket.bucketRange.distance).isEqualTo(1.0)
    assertThat(bucket.bucketRange.entriesCount).isEqualTo(100)
    assertThat(bucket.bucketRange.duration).isEqualTo(100.0)
    assertThat(bucket.chunk.timeStampsCount).isEqualTo(100)

    val parentDescriptor = descriptor.parent()!!
    assertThat(parentDescriptor.bucketRange).isEqualTo(HistoryBucketRange.FiveSeconds)
    assertThat(parentDescriptor.bucketRange.samplingPeriod).isEqualTo(SamplingPeriod.EveryTenMillis)
    assertThat(parentDescriptor.bucketRange.duration).isEqualTo(5000.0)
    assertThat(parentDescriptor.bucketRange.entriesCount).isEqualTo(500)

    assertThat(parentDescriptor.start).isEqualTo(descriptor.start)

    assertThat(parentDescriptor.bucketRange.downSamplingFactor()).isEqualTo(10)
  }
}

//(sin(timeStamps[it] / 1_000.000) * 100).toInt()


fun Assert<HistoryEnumSet>.isPending(): Unit = given {
  if (it.isPending()) return
  expected("to be Pending but was ${show(it)}")
}

fun Assert<HistoryEnumSet>.isNoValue(): Unit = given {
  if (it.isNoValue()) return
  expected("to be NoValue but was ${show(it)}")
}
