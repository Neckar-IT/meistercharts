/*
 * Copyright (C) 2013-2026 Neckar IT GmbH, Mössingen, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library. Thus, the terms and
 * conditions of the GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting combined work under terms of your choice, provided that every
 * copy of the combined work is accompanied by a complete copy of the source
 * code of this library.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package it.neckar.open.retry

import it.neckar.logging.LoggerFactory
import kotlinx.coroutines.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Retries something several times
 */
suspend fun <T> retry(
  retryCount: Int = 3,

  initialDelay: Duration = 100.milliseconds,

  maxDelay: Duration = 1.seconds,
  delayIncreaseFactor: Double = 2.0,

  /**
   * Is called for each exception - for each of the retries.
   * Is *not* called for the last try - instead the last exception is thrown
   */
  exceptionHandler: (e: Exception, tryIndex: Int) -> Unit = { e: Exception, tryIndex: Int ->
    logger.info("Exception thrown in try $tryIndex: $e")
  },

  /**
   * The block that is executed
   */
  block: suspend (tryIndex: Int) -> T,
): T {
  contract {
    callsInPlace(block, InvocationKind.AT_LEAST_ONCE)
  }

  var currentDelay = initialDelay
  repeat(retryCount - 1) { tryIndex -> //last try at the bottom
    try {
      return block(tryIndex)
    } catch (c: CancellationException) {
      //Always cancel
      throw c
    } catch (t: Exception) {
      exceptionHandler(t, tryIndex)
    }

    delay(currentDelay)
    currentDelay = (currentDelay * delayIncreaseFactor).coerceAtMost(maxDelay)
  }

  return block(retryCount - 1) //last try (matches the loop's tryIndex sequence)
}

private val logger = LoggerFactory.getLogger("it.neckar.open.retry.retry")
