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
package com.meistercharts.fx

import assertk.*
import assertk.assertions.*
import com.meistercharts.zoom.ZoomAndTranslationDefaults
import it.neckar.open.javafx.JavaFxTimer
import it.neckar.open.dispose.Disposable
import it.neckar.open.javafx.test.JavaFxTest
import javafx.application.Platform
import kotlinx.coroutines.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 *
 */
@JavaFxTest
class MeisterChartBuilderFXTest {
  @BeforeEach
  fun setUp() {
    Platform.runLater {
      assertThat(Thread.currentThread().name).isEqualTo("JavaFX Application Thread")
    }

    Thread.setDefaultUncaughtExceptionHandler { t, e ->
      System.err.println("Uncaught exception in " + t)
      System.err.println(e.stackTraceToString())
    }
  }

  @AfterEach
  fun tearDown() {
    Dispatchers.Main.cancel()

    //Ensure that the UI thread has been renamed
    JavaFxTimer.runAndWait {
    }
  }

  @Test
  suspend fun testContracts() {
    withContext(Dispatchers.Main) {
      MeisterChartsPlatform.init()
    }

    val meisterChartBuilder = MeisterchartBuilderFX(javaClass.simpleName)

    val a: Int

    meisterChartBuilder.zoomAndTranslationDefaults {
      a = 17
      ZoomAndTranslationDefaults.noTranslation
    }

    assertThat(a).isEqualTo(17)
  }

  @Test
  suspend fun testDisposable() {
    withContext(Dispatchers.Main) {
      MeisterChartsPlatform.init()

      val meisterChartBuilder = MeisterchartBuilderFX(javaClass.simpleName)

      val disposable = object : Disposable {
        var disposeCalled = false

        override fun dispose() {
          assertThat(disposeCalled).isFalse()
          disposeCalled = true
        }
      }
      meisterChartBuilder.onDispose(disposable)

      assertThat(disposable.disposeCalled).isFalse()
      val meisterChart = meisterChartBuilder.build()
      assertThat(disposable.disposeCalled).isFalse()

      meisterChart.dispose()
      assertThat(disposable.disposeCalled).isTrue()
    }
  }
}
