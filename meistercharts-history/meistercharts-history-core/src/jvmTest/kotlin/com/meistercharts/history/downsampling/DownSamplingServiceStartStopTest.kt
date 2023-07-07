package com.meistercharts.history.downsampling

import assertk.*
import assertk.assertions.*
import com.meistercharts.history.InMemoryHistoryStorage
import it.neckar.open.time.JVMTimerCoroutineSupport
import it.neckar.open.time.jvmTimerSupport
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class DownSamplingServiceStartStopTest {
  @Test
  fun testIt(): Unit = runTest {
    val context = StandardTestDispatcher(testScheduler)

    val timerScope = TestScope()
    jvmTimerSupport = JVMTimerCoroutineSupport(CoroutineScope(context), CoroutineScope(context))

    var called = false

    val historyStorage = InMemoryHistoryStorage()
    val service = object : DownSamplingService<InMemoryHistoryStorage>(historyStorage) {
      override fun calculateDownSamplingIfRequired(downSamplingDirtyRangesCollector: DownSamplingDirtyRangesCollector) {
        called = true
        throw UnsupportedOperationException("must not be called in this test")
      }
    }

    assertThat(service.downSamplingScheduled).isFalse()

    val disposable = service.scheduleDownSampling()
    assertThat(service.downSamplingScheduled).isTrue()


    disposable.dispose()
    assertThat(service.downSamplingScheduled).isFalse()

    timerScope.advanceTimeBy(10_000.seconds)

    assertThat(called).isFalse()
  }
}
