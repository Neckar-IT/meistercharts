package com.meistercharts.algorithms.time

import com.meistercharts.algorithms.time.TimeZoneOffsetProvider

/**
 * Computes the 'real' time-zone offset for a given timestamp and a given time-zone
 */
expect class DefaultTimeZoneOffsetProvider() : TimeZoneOffsetProvider
