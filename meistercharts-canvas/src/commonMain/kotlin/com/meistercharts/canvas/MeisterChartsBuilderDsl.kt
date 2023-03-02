package com.meistercharts.canvas

/**
 * Annotations that is used to ensure the correct DSL calls are made when configuring charts
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.TYPEALIAS)
@DslMarker
annotation class MeisterChartsBuilderDsl
