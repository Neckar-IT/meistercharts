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
 * JS implementation - always inert.
 *
 * The browser offers no generic hook to observe allocations by type at runtime, so recording is not
 * supported. [mode] can still be set (the interactive toggle is cross platform), but [currentReport]
 * always returns [AllocationReport.empty].
 */
actual object AllocationRecordingEngine {
  actual var mode: AllocationRecordingMode = AllocationRecordingMode.Off

  actual fun currentReport(): AllocationReport {
    return AllocationReport.empty
  }

  actual fun reset() {
  }
}
