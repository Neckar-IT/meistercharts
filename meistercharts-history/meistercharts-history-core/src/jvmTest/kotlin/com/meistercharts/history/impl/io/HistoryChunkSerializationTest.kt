package com.meistercharts.history.impl.io

/**
 */
//class HistoryChunkSerializationTest {
//  @Test
//  fun testIt() {
//    val timeStamps = doubleArrayOf(1001.0, 1002.0, 1003.0)
//    val dataSeriesIds = intArrayOf(10, 11, 12, 13)
//    val decimalPlaces = byteArrayOf(1, 2, 1, 0)
//    val displayNames = arrayOf(
//      TextKey("temp", "Temperature"),
//      TextKey("height", "Height"),
//      TextKey("temp2", "Temperature 2"),
//      TextKey("temp3", "Temperature 3")
//    )
//
//    val values = HistoryValues(
//      arrayOf(
//        intArrayOf(1, 2, 3),
//        intArrayOf(10, 20, 30),
//        intArrayOf(100, 200, 300),
//        intArrayOf(1000, 2000, 3000)
//      )
//    )
//
//    val chunk = HistoryChunk(dataSeriesIds, timeStamps, values, decimalPlaces, displayNames)
//    assertThat(chunk).isNotNull()
//
//
//    val jsonProcessor = Json.indented
//
//    val serializer = HistoryChunk.serializer()
//    val json = jsonProcessor.toJson(serializer, chunk)
//    println("$json")
//
//    val parsedFromJson = jsonProcessor.fromJson(serializer, json)
//    assertThat(parsedFromJson).isEqualTo(chunk)
//
//
//    val protBuf = ProtoBuf.dump(serializer, chunk)
//
//    val parsedFromProtoBuf = ProtoBuf.load(serializer, protBuf)
//    assertThat(parsedFromProtoBuf).isEqualTo(chunk)
//  }
//
//  @Test
//  fun testBigger() {
//    val count = 1000
//
//    val timeStamps = DoubleArray(count)
//
//    val dataSeriesIds = intArrayOf(10, 11, 12, 13)
//    val decimalPlaces = byteArrayOf(1, 2, 1, 0)
//    val displayNames = arrayOf(
//      TextKey("temp", "Temperature"),
//      TextKey("height", "Height"),
//      TextKey("temp2", "Temperature 2"),
//      TextKey("temp3", "Temperature 3")
//    )
//
//    val values0 = IntArray(count)
//    val values1 = IntArray(count)
//    val values2 = IntArray(count)
//    val values3 = IntArray(count)
//
//    for (i in 0 until count) {
//      timeStamps[i] = i * 1000.0
//
//      values0[i] = i
//      values1[i] = i * 10
//      values2[i] = i * 100
//      values3[i] = i * 1000
//    }
//
//    val values: HistoryValues = HistoryValues(
//      arrayOf(
//        values0, values1, values2, values3
//      )
//    )
//
//    val chunk = HistoryChunk(dataSeriesIds, timeStamps, values, decimalPlaces, displayNames)
//    assertThat(chunk).isNotNull
//
//
//    val json = Json.stringify(HistoryChunk.serializer(), chunk)
//    println("Json: $json")
//    assertThat(json.length).isLessThan(31_000)
//
//    val protBuf = ProtoBuf.dump(HistoryChunk.serializer(), chunk)
//    //assertThat(protBuf.size).isEqualTo(22_779)
//  }
//
//}
