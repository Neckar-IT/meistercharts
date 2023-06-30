package com.meistercharts.loop

import it.neckar.open.unit.number.IsFinite
import it.neckar.open.unit.other.Relative
import it.neckar.open.unit.si.ms

/**
 * Is notified on every render loop
 */
fun interface RenderLoopListener {
  fun render(
    /**
     * The current time (absolute) for this frame.
     * Do *not* use this value for animations.
     */
    frameTimestamp: @ms @IsFinite Double,

    /**
     * The relative time - with higher precision.
     * This value should be used for animations.
     */
    relativeHighRes: @ms @Relative Double,
  )
}
