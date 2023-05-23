package it.neckar.open.test.utils

import org.awaitility.core.ConditionFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 */
fun ConditionFactory.untilAtomicIsTrue(atomicBoolean: AtomicBoolean) {
  return untilAtomic(atomicBoolean, org.hamcrest.CoreMatchers.`is`(true))
}

fun ConditionFactory.atMostMillis(millis: Long): ConditionFactory {
  return atMost(millis, TimeUnit.MILLISECONDS)
}
