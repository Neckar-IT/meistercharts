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
import assertk.*
import assertk.assertions.*
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import it.neckar.csv.loadCSV
import kotlinx.browser.window
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.Ignore
import kotlin.test.Test

class CsvLoadingTest {

  /**
   * Resource loading fails in Kotlin/JS tests.
   *
   * workaround: dependency=libs:kotlin:2.4.20 observed-on=2026-10-07 upstream=https://youtrack.jetbrains.com/issue/KT-42923
   */
  @Ignore
  @Test
  fun testReadCsv() = runTest {
    withTimeout(1000) {
      val csv = loadCSV("data/SterbestatistikDE.csv")
      println("CSV: ")
      println(csv)
      assertThat(csv).isNotNull()
    }
  }



}



