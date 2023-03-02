package com.meistercharts.algorithms

/**
 * Base class for delegating chart states.
 */
abstract class DelegatingChartState(val delegate: ChartState) : ChartState by delegate
