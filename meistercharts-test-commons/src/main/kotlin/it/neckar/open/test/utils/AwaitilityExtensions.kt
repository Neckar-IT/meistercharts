package it.neckar.open.test.utils

import kotlinx.coroutines.*
import org.awaitility.core.ConditionFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 */
@Deprecated("use untilTrue(). Do not use this in coroutines, use AtomicBoolean.waitUntilTrue() instead!", ReplaceWith("untilTrue(atomicBoolean)"))
fun ConditionFactory.untilAtomicIsTrue(atomicBoolean: AtomicBoolean) {
  return untilTrue(atomicBoolean)
}

fun ConditionFactory.atMostMillis(millis: Long): ConditionFactory {
  return atMost(millis, TimeUnit.MILLISECONDS)
}


/**
 * Delays the thread until the Atomic Boolean is true.
 */
suspend fun AtomicBoolean.waitUntilTrue(timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  this.waitUntil(true, timeoutDuration, delay)
}

/**
 * Delays the thread until the Atomic Boolean is false.
 */
suspend fun AtomicBoolean.waitUntilFalse(timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  this.waitUntil(false, timeoutDuration, delay)
}

/**
 * Waits until the expectedValue is achieved.
 */
suspend fun AtomicBoolean.waitUntil(expectedValue: Boolean, timeoutDuration: Duration = 10.seconds, delay: Duration = 10.milliseconds): Unit {
  withTimeout(timeoutDuration) {
    while (isActive && get() != expectedValue) {
      delay(delay)
    }
  }
}
