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
package com.meistercharts.history.impl.io

import com.meistercharts.history.HistoryBucket
import com.meistercharts.history.HistoryBucketDescriptor
import com.meistercharts.history.HistoryBucketRange
import com.meistercharts.history.impl.createSinusChunk
import it.neckar.open.serialization.roundTrip
import org.junit.jupiter.api.Test

/**
 */
class HistoryBucketSerializerTest {
  @Test
  fun testIt() {
    val descriptor = HistoryBucketDescriptor.forTimestamp(351351351.0, HistoryBucketRange.OneDay)
    val bucket = HistoryBucket(descriptor, createSinusChunk(descriptor))

    roundTrip(bucket, HistoryBucket.serializer()) {
      //language=JSON
      """
        {
          "descriptor" : {
            "index" : 4.0,
            "bucketRange" : "OneDay"
          },
          "chunk" : {
            "configuration" : {
              "decimalConfiguration" : {
                "dataSeriesIds" : [ 10, 11, 12 ],
                "displayNames" : [ {
                  "key" : "val1",
                  "fallbackText" : "Value 1"
                }, {
                  "key" : "val2",
                  "fallbackText" : "Value 2"
                }, {
                  "key" : "val3",
                  "fallbackText" : "Value 3"
                } ],
                "units" : [ "kg", "cm", null ]
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
            "timeStamps" : [ 3.456E8, 3.462E8, 3.468E8, 3.474E8, 3.48E8, 3.486E8, 3.492E8, 3.498E8, 3.504E8, 3.51E8, 3.516E8, 3.522E8, 3.528E8, 3.534E8, 3.54E8, 3.546E8, 3.552E8, 3.558E8, 3.564E8, 3.57E8, 3.576E8, 3.582E8, 3.588E8, 3.594E8, 3.6E8, 3.606E8, 3.612E8, 3.618E8, 3.624E8, 3.63E8, 3.636E8, 3.642E8, 3.648E8, 3.654E8, 3.66E8, 3.666E8, 3.672E8, 3.678E8, 3.684E8, 3.69E8, 3.696E8, 3.702E8, 3.708E8, 3.714E8, 3.72E8, 3.726E8, 3.732E8, 3.738E8, 3.744E8, 3.75E8, 3.756E8, 3.762E8, 3.768E8, 3.774E8, 3.78E8, 3.786E8, 3.792E8, 3.798E8, 3.804E8, 3.81E8, 3.816E8, 3.822E8, 3.828E8, 3.834E8, 3.84E8, 3.846E8, 3.852E8, 3.858E8, 3.864E8, 3.87E8, 3.876E8, 3.882E8, 3.888E8, 3.894E8, 3.9E8, 3.906E8, 3.912E8, 3.918E8, 3.924E8, 3.93E8, 3.936E8, 3.942E8, 3.948E8, 3.954E8, 3.96E8, 3.966E8, 3.972E8, 3.978E8, 3.984E8, 3.99E8, 3.996E8, 4.002E8, 4.008E8, 4.014E8, 4.02E8, 4.026E8, 4.032E8, 4.038E8, 4.044E8, 4.05E8, 4.056E8, 4.062E8, 4.068E8, 4.074E8, 4.08E8, 4.086E8, 4.092E8, 4.098E8, 4.104E8, 4.11E8, 4.116E8, 4.122E8, 4.128E8, 4.134E8, 4.14E8, 4.146E8, 4.152E8, 4.158E8, 4.164E8, 4.17E8, 4.176E8, 4.182E8, 4.188E8, 4.194E8, 4.2E8, 4.206E8, 4.212E8, 4.218E8, 4.224E8, 4.23E8, 4.236E8, 4.242E8, 4.248E8, 4.254E8, 4.26E8, 4.266E8, 4.272E8, 4.278E8, 4.284E8, 4.29E8, 4.296E8, 4.302E8, 4.308E8, 4.314E8 ],
            "values" : {
              "decimalHistoryValues" : {
                "values" : "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",
                "minValues" : null,
                "maxValues" : null
              },
              "enumHistoryValues" : {
                "values" : "AAAAkA==",
                "mostOfTheTimeValues" : null
              },
              "referenceEntryHistoryValues" : {
                "values" : "AAAAkA==",
                "differentIdsCount" : null,
                "statuses" : "AAAAkA==",
                "dataMap" : {
                  "type" : "Default",
                  "entries" : { }
                }
              }
            },
            "recordingType" : "Measured"
          }
        }
      """.trimIndent()
    }
  }
}
