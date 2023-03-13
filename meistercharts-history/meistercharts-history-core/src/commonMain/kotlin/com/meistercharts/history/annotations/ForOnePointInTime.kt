package com.meistercharts.history.annotations

/**
 * Things marked with this annotation happen at exactly one point in time.
 *
 *
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class ForOnePointInTime()
