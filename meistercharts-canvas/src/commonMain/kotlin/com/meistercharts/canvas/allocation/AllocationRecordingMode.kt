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
package com.meistercharts.canvas.allocation

/**
 * Controls how allocations are recorded during a paint.
 *
 * Recording only produces data on the JVM and only if the process was started with the
 * java-allocation-instrumenter agent (`-javaagent:...`). Without the agent - and on JS - the
 * recording is inert (empty reports), see [AllocationRecordingEngine].
 */
enum class AllocationRecordingMode {
  /**
   * No recording. This is the default and has no runtime cost.
   */
  Off,

  /**
   * Records the count and byte size per allocated type.
   */
  ByType,

  /**
   * Records the count and byte size per allocated type and additionally captures the stacktrace
   * of each allocation. Significantly more expensive than [ByType].
   */
  ByTypeAndStacktrace,

  ;

  /**
   * Whether allocations are recorded in this mode
   */
  val recording: Boolean
    get() = this != Off

  /**
   * Whether the stacktrace is captured for each allocation
   */
  val capturesStacktrace: Boolean
    get() = this == ByTypeAndStacktrace

  /**
   * Returns the next mode - cycles back to [Off]. Used to toggle through the modes interactively.
   */
  fun next(): AllocationRecordingMode {
    return entries[(ordinal + 1) % entries.size]
  }
}
