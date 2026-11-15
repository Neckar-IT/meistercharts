/*
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
package com.meistercharts.demo.descriptors

import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import it.neckar.csv.loadCSV
import kotlinx.coroutines.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.math.floor

class CsvLoadingCombiningTest {
  @Disabled
  @Test
  fun testMakeCombinedCsv() {
    runBlocking {
      val popCsv = loadCSV("data/BevölkerungsstandDE.csv")
      val birthCsv = loadCSV("data/StatistikGeburtenDE.csv")
      val deathCsv = loadCSV("data/SterbestatistikDE.csv")
      val writer = csvWriter{
        delimiter =';'
        lineTerminator = "\n"
        charset = "UTF-8"
      }
    writer.open("src/commonMain/resources/data/Combined.csv"){
      for (i in 0..877) {

        // calculating missing values of population in a  linear manner
        var popValue = 0
        val monthInt = (i + 1) % 12 // 1 is Jan, 0 is Dec

        if (i < 12 ) {
          popValue = popCsv[0][1].toInt()
        } else if(i > 863){
          val lastIndex = popCsv.size - 1
          popValue = popCsv[lastIndex][1].toInt()
        } else {
          if (monthInt == 0) {
            popValue = popCsv[(i / 12)][1].toInt()
          } else {
            println("___________________________")
            println("index: $i")
            println("monthInt: $monthInt")

            // diff: pipCsv[1][23] - pipCsv[1][11]
            val lastIndex = floor(i / 12.0).toInt() - 1
            val lastValue = popCsv[lastIndex][1].toInt()
            println("last index: $lastIndex")
            println("last value: $lastValue")

            val nextIndex = lastIndex + 1
            val nextValue = popCsv[nextIndex][1].toInt()
            println("next index: $nextIndex")
            println("next value: $nextValue")


            val diff = nextValue - lastValue
            println("diff: $diff")
            val percentage: Double = monthInt / 12.0
            println("percentage: $percentage")

            popValue = lastValue + (percentage * diff).toInt()
            println("new popValue: $popValue")

          }
        }
        val deathsTotal = deathCsv[i][2].toInt() + deathCsv[i][3].toInt()

        // creating row
        // Jahr, Monat, Geburten M , Geburten F, Geburten Gesamt, Tode M, Tode F, Tode Gesamt, Bevölkerungsstand
        val row = listOf(
          birthCsv[i][0], birthCsv[i][1], birthCsv[i][2], birthCsv[i][3], birthCsv[i][4],
          deathCsv[i][2], deathCsv[i][3], deathsTotal,
          popValue
        )
        writeRow(row)
      }
    }

    }
  }
}
