/*
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
     * The "now" of this frame for time axes and translation over time; never for animations.
     */
    frameTimestamp: @ms @IsFinite Double,

    /**
     * The [it.neckar.open.time.monotonicMillis] of this frame; drives every animation.
     */
    frameMonotonicMillis: @ms Double,

    /**
     * The platform's display refresh time; keeps render throttling on real time while demos pause the virtual time.
     */
    relativeHighRes: @ms @Relative Double,
  )
}
