package com.meistercharts.annotations

import it.neckar.open.unit.other.pct

/**
 * The value as percentage within the content area.
 * Respects the orientation of the axis [com.meistercharts.algorithms.AxisOrientationX] and [com.meistercharts.algorithms.AxisOrientationY]
 *
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
  AnnotationTarget.CLASS,
  AnnotationTarget.ANNOTATION_CLASS,
  AnnotationTarget.TYPE_PARAMETER,
  AnnotationTarget.PROPERTY,
  AnnotationTarget.FIELD,
  AnnotationTarget.LOCAL_VARIABLE,
  AnnotationTarget.VALUE_PARAMETER,
  AnnotationTarget.CONSTRUCTOR,
  AnnotationTarget.FUNCTION,
  AnnotationTarget.PROPERTY_GETTER,
  AnnotationTarget.PROPERTY_SETTER,
  AnnotationTarget.TYPE,
  AnnotationTarget.EXPRESSION,
  AnnotationTarget.FILE,
  AnnotationTarget.TYPEALIAS
)
@MustBeDocumented
@pct
annotation class ContentAreaRelative
