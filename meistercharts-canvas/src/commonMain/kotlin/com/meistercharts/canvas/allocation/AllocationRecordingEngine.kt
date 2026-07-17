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
 * Records - while enabled - which objects are allocated during layer paints.
 *
 * The [mode] is process wide. Setting it to a recording mode starts sampling, setting it back to
 * [AllocationRecordingMode.Off] stops it and clears the accumulated data. [currentReport] returns a
 * snapshot of everything sampled since recording was (re)started.
 *
 * Platform behavior:
 * - JVM: uses JFR (`jdk.ObjectAllocationSample`). No agent, no `-javaagent`, no restart - a plain JDK
 *   API that can be toggled at runtime in a shipped build. Samples are attributed to a layer via
 *   their stacktrace (which `Layer.paint` is on the stack), so only allocations that happen during a
 *   layer paint are counted. ATTENTION: JFR *samples* - counts are relative, not exact.
 * - JS: always inert. The browser offers no generic allocation hook. [mode] can be set (the toggle is
 *   cross platform), but [currentReport] always returns [AllocationReport.empty].
 */
expect object AllocationRecordingEngine {
  /**
   * The current recording mode. Defaults to [AllocationRecordingMode.Off].
   */
  var mode: AllocationRecordingMode

  /**
   * Returns a snapshot of the allocations sampled since recording was (re)started.
   * Returns [AllocationReport.empty] when [mode] is [AllocationRecordingMode.Off].
   */
  fun currentReport(): AllocationReport

  /**
   * Clears the accumulated samples without stopping the recording.
   */
  fun reset()
}
