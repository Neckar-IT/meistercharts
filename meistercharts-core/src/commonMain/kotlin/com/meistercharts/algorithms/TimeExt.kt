package com.meistercharts.algorithms

import it.neckar.open.provider.DoubleProvider
import it.neckar.open.unit.si.ms

/**
 * Double provider that returns the current time in millis
 */
val nowMillisProvider: @ms DoubleProvider = DoubleProvider.nowMillis
