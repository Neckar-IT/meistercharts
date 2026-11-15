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
package com.meistercharts.demo

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.formatting.formatUtc
import it.neckar.open.time.ClockNowProvider
import it.neckar.open.time.VirtualNowProvider
import it.neckar.open.time.monotonicTimeSource
import it.neckar.open.time.nowProvider
import it.neckar.open.unit.si.ms

/**
 * How the virtual time should be handled for a demo
 */
interface DemoVirtualTimeHandler {
  /**
   * Returns the initial now for the time provider
   */
  fun initialNow(): @ms Double

  /**
   * Is called regularly to update the virtual time provider
   */
  fun updateVirtualNowProvider(virtualNowProvider: VirtualNowProvider): VirtualNowState

  /**
   * The state of the virtual now provider
   */
  enum class VirtualNowState {
    /**
     * The virtual now provider is running - time is progressing
     */
    Progressing,

    /**
     * The virtual now provider is paused - time is not progressing (anymore)
     */
    Paused
  }

  companion object {
    /**
     * Updates virtual now provider with the current time.
     * "Feels" like normal time
     */
    val Default: DemoVirtualTimeHandler = object : DemoVirtualTimeHandler {
      override fun initialNow(): Double {
        return ClockNowProvider.nowMillis()
      }

      override fun updateVirtualNowProvider(virtualNowProvider: VirtualNowProvider): VirtualNowState {
        virtualNowProvider.updateVirtualNow()
        return VirtualNowState.Progressing
      }
    }

    /**
     * Creates a demo virtual time handler with a fixed offset relative to the "real" time
     */
    fun fixedOffset(initialTime: @ms Double = DemoReferenceTime, timeOffset: @ms Double): DemoVirtualTimeHandler {
      return object : DemoVirtualTimeHandler {
        override fun initialNow(): Double {
          return initialTime
        }

        override fun updateVirtualNowProvider(virtualNowProvider: VirtualNowProvider): VirtualNowState {
          virtualNowProvider.virtualNow = initialTime + timeOffset
          return VirtualNowState.Paused
        }
      }
    }

    /**
     * Creates a virtual time handler that approaches the target offset step by step.
     * This is useful for demos that do work after a given amount of time.
     *
     * @param initialTime The initial time
     * @param targetOffset The target offset - where the time will pause
     * @param timeProgressionStep The step size for the time progression. If null, a default value of 250ms is used
     */
    fun fixedOffsetStepByStep(initialTime: @ms Double = DemoReferenceTime, targetOffset: @ms Double, timeProgressionStep: @ms Double? = null): DemoVirtualTimeHandler {
      return object : DemoVirtualTimeHandler {
        val timeProgressionStepDefaultValue = 250.0

        override fun initialNow(): Double {
          return initialTime
        }

        override fun updateVirtualNowProvider(virtualNowProvider: VirtualNowProvider): VirtualNowState {
          @ms val oldVirtualNow = virtualNowProvider.virtualNow
          @ms val newVirtualNow = (oldVirtualNow + (timeProgressionStep ?: timeProgressionStepDefaultValue)).coerceAtMost(initialTime + targetOffset)

          if (oldVirtualNow != newVirtualNow) {
            logger.debug("Updating virtual now from ${oldVirtualNow.formatUtc()} to ${newVirtualNow.formatUtc()}")
            virtualNowProvider.virtualNow = newVirtualNow
            return VirtualNowState.Progressing
          }

          logger.trace("Target time reached, no update necessary")
          return VirtualNowState.Paused
        }

        private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.demo.DemoVirtualTimeHandler")
      }
    }

    /**
     * Sets the virtual now provider to a fixed time.
     * Usually [fixedOffsetStepByStep] should be used instead
     */
    fun fixed(time: @ms Double): DemoVirtualTimeHandler {
      return object : DemoVirtualTimeHandler {
        override fun updateVirtualNowProvider(virtualNowProvider: VirtualNowProvider): VirtualNowState {
          virtualNowProvider.virtualNow = time
          return VirtualNowState.Paused
        }

        override fun initialNow(): Double {
          return time
        }
      }
    }

    /**
     * The base reference time that is the default for demos.
     * ATTENTION! This value will be changed without warning!
     */
    const val DemoReferenceTime: @ms Double = (1686042664231.0) //2023-06-06T09:11:04.231
  }
}


/**
 * Registers a virtual now handler for the demo
 */
fun MeisterchartsDemo.registerVirtualNowHandlerForDemos(
  demoVirtualTimeHandler: DemoVirtualTimeHandler,
  /**
   * The callback that is called when the demo is paused
   *
   * Attention: This callback is called only once!
   */
  callbackOnPause: () -> Unit,
) {
  //Use the virtual now provider with a fixed start date - for points in time and measured durations alike
  val virtualNowProvider = VirtualNowProvider(demoVirtualTimeHandler.initialNow()).also {
    nowProvider = it
    monotonicTimeSource = it
  }

  meistercharts {
    configure {
      var pauseCalled = false

      chartSupport.onRenderPrepend { _, _, _ ->
        val virtualNowState: DemoVirtualTimeHandler.VirtualNowState = demoVirtualTimeHandler.updateVirtualNowProvider(virtualNowProvider)

        if (virtualNowState == DemoVirtualTimeHandler.VirtualNowState.Paused) {
          //Notify everybody that the demo is paused

          if (pauseCalled.not()) {
            pauseCalled = true
            callbackOnPause()
          }
        }
      }
    }
  }
}
