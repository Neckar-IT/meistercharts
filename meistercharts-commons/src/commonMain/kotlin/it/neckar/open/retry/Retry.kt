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

  return block(retryCount) //last try
}

private val logger = LoggerFactory.getLogger("it.neckar.open.retry.retry")
