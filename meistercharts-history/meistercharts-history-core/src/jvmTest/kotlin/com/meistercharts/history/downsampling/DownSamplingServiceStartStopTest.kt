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
import com.meistercharts.history.InMemoryHistoryStorage
import it.neckar.open.time.JVMTimerCoroutineImplementation
import it.neckar.open.time.timerImplementation
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds

class DownSamplingServiceStartStopTest {
  @Test
  fun testIt(): Unit = runTest {
    val context = StandardTestDispatcher(testScheduler)

    val timerScope = TestScope()
    timerImplementation = JVMTimerCoroutineImplementation(CoroutineScope(context), CoroutineScope(context))

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
