package com.meistercharts.demo.descriptors.history

import assertk.*
import assertk.assertions.*
import it.neckar.open.kotlin.lang.fastFor
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 *
 */
class DataCsvDemo {
  @Test
  fun testReadCsv() {
    val resource = javaClass.getResourceAsStream("/data/airport.csv")

    val asdf = "asdf".split('1')

    assertThat(resource).isNotNull()

    csvReader {
      skipEmptyLine = true
    }.open(resource) {
      100.fastFor { index ->
        val line = readNext() ?: return@fastFor

        if (index > 100) {
          return@open
        }

        val dateTimeAsString = line[1]
        if (dateTimeAsString == "DATE") {
          //Skip headline
          return@fastFor
        }

        val dateTime = LocalDateTime.parse(dateTimeAsString)
        val altimeterSetting = line[26 + 16 - 1].toDoubleOrNull()
        val relativeHumidity = line[26 + 23 - 1].toDoubleOrNull()
        val seaLevelPressure = line[26 + 24 - 1].toDoubleOrNull()
        val stationPressure = line[26 + 26 - 1].toDoubleOrNull()
        val windDirection = line[26 * 2 + 3 - 1].toIntOrNull()
        val windSpeed = line[26 * 2 + 5 - 1].toDoubleOrNull()

        println("$dateTime: $altimeterSetting $relativeHumidity - $seaLevelPressure -$stationPressure - $windDirection $windSpeed")
      }
    }


  }

  @Test
  fun testParseDate() {
    val string = "2011-01-01T07:53:00"
    val parsed = LocalDateTime.parse(string)
    assertThat(parsed).isEqualTo(LocalDateTime.of(2011, 1, 1, 7, 53, 0))
  }
}
