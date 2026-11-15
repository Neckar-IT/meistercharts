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

import assertk.*
import assertk.assertions.*
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.junit.jupiter.api.Test
import java.io.File

class CsvLoadingTest {
  @Test
  fun testReadCsv() {
    val file: File = File("src/commonMain/resources/data/SterbestatistikDE.csv")
    val rows: List<List<String>> = csvReader().readAll(file)
    assertThat(rows).hasSize(878)
  }
}
