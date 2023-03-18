package com.meistercharts.demo.descriptors

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.InMemoryHistoryStorage
import com.meistercharts.history.ReferenceEntryId
import com.meistercharts.history.SamplingPeriod
import com.meistercharts.history.generator.HistoryChunkGenerator
import it.neckar.open.test.utils.isEqualComparingLinesTrim
import it.neckar.open.time.JVMTimerCoroutineSupport
import it.neckar.open.time.jvmTimerSupport
import it.neckar.open.unit.si.ms
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HistoryReferenceScenariosTest {
  lateinit var ciBuilds: HistoryReferenceScenarios.CiBuilds

  @BeforeEach
  fun setUp() {
    jvmTimerSupport = JVMTimerCoroutineSupport()
    ciBuilds = HistoryReferenceScenarios.CiBuilds()
  }

  @AfterEach
  fun tearDown() {
    ciBuilds.dispose()
  }

  @Test
  fun testIt() {
    val historyConfiguration = ciBuilds.historyConfiguration
    assertThat(historyConfiguration.totalDataSeriesCount).isEqualTo(12)

    assertThat(historyConfiguration.dump()).isEqualComparingLinesTrim(
      """
        Decimals:
             11: Worker 1: CPU Temperature | (°C)
             12: Worker 2: CPU Temperature | (°C)
             13: Worker 3: CPU Temperature | (°C)
             21: Worker 1: CPU %           | (percent)
             22: Worker 2: CPU %           | (percent)
             23: Worker 3: CPU %           | (percent)

        Enums:
             31: Worker 1: State           | (Worker State)
             32: Worker 2: State           | (Worker State)
             33: Worker 3: State           | (Worker State)

        Reference Entries:
             41: Worker 1: Job             | (Job State)
             42: Worker 2: Job             | (Job State)
             43: Worker 3: Job             | (Job State)
    """.trimIndent()
    )

    val historyStorage = InMemoryHistoryStorage()

    HistoryChunkGenerator(
      historyStorage = historyStorage,
      samplingPeriod = SamplingPeriod.EverySecond,
      decimalValueGenerators = ciBuilds.decimalGenerators,
      enumValueGenerators = ciBuilds.enumGenerators,
      referenceEntryGenerators = ciBuilds.referenceEntryGenerators,

      referenceEntryStatusProvider = { referenceEntryId: ReferenceEntryId, millis: @ms Double ->
        TODO()
      },
      historyConfiguration = historyConfiguration
    )

  }
}
