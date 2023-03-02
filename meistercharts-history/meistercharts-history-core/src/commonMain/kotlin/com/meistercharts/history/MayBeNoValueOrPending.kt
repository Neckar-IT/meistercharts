package com.meistercharts.history

/**
 * Values annotated might be NoValue or Pending
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.LOCAL_VARIABLE)
@MustBeDocumented
@Retention(AnnotationRetention.SOURCE)
annotation class MayBeNoValueOrPending()
