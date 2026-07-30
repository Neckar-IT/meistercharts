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

import it.neckar.logging.Logger
import it.neckar.logging.LoggerFactory
import it.neckar.open.threads.isFxApplicationThreadName
import jdk.jfr.consumer.RecordedEvent
import jdk.jfr.consumer.RecordedFrame
import jdk.jfr.consumer.RecordedThread
import jdk.jfr.consumer.RecordingStream

/**
 * JVM implementation backed by JFR (`jdk.ObjectAllocationSample`).
 *
 * When [mode] becomes a recording mode a background [RecordingStream] is started that samples object
 * allocations for the whole process. Samples are kept only if they happened on the paint thread (the
 * JavaFX Application Thread, see [RecordedThread.isFxApplicationThread]) and are attributed to a layer
 * via their stacktrace (the `Layer.layout`/`Layer.paint` frame closest to the root of the stack - both
 * render phases count). Setting [mode] back to [AllocationRecordingMode.Off] stops the stream.
 *
 * ATTENTION: JFR *samples* allocations - the reported counts are relative, not exact per-frame counts.
 */
actual object AllocationRecordingEngine {
  private val lock = Any()

  /**
   * layerName -> (typeName -> accumulator)
   */
  private val aggregate = HashMap<String, HashMap<String, MutableTypeAllocation>>()

  private var stream: RecordingStream? = null

  @Volatile
  private var currentMode: AllocationRecordingMode = AllocationRecordingMode.Off

  actual var mode: AllocationRecordingMode
    get() = currentMode
    set(value) {
      synchronized(lock) {
        if (currentMode == value) {
          return
        }
        currentMode = value
        if (value.recording) {
          startRecording()
        } else {
          stopRecording()
        }
      }
    }

  actual fun currentReport(): AllocationReport {
    synchronized(lock) {
      if (aggregate.isEmpty()) {
        return AllocationReport.empty
      }

      val layerAllocations = aggregate
        .map { (layerName, types) ->
          LayerAllocations(
            layerName = layerName,
            allocationsByType = types.values
              .map { it.toTypeAllocation() }
              .sortedByDescending { it.samples },
          )
        }
        .sortedByDescending { it.totalSamples }

      return AllocationReport(layerAllocations)
    }
  }

  actual fun reset() {
    synchronized(lock) {
      aggregate.clear()
    }
  }

  private fun startRecording() {
    if (stream != null) {
      return
    }
    aggregate.clear()

    try {
      val newStream = RecordingStream()
      newStream.enable("jdk.ObjectAllocationSample").withStackTrace()
      newStream.onEvent("jdk.ObjectAllocationSample") { event -> onSample(event) }
      newStream.startAsync()
      stream = newStream
    } catch (e: Throwable) {
      //JFR not available on this JVM - stay inert
      logger.debug("Could not start the JFR recording stream, allocation recording stays inert", e)
      stream = null
    }
  }

  private fun stopRecording() {
    stream?.let { runningStream ->
      runCatching { runningStream.close() }
    }
    stream = null
    aggregate.clear()
  }

  private fun onSample(event: RecordedEvent) {
    val activeMode = currentMode
    if (activeMode.recording.not()) {
      return
    }

    //Reject anything that is not on the paint thread. On the JVM MeisterCharts paints on the JavaFX
    //Application Thread, so a stray off-thread allocation cannot leak in even if it carried a
    //Layer.paint frame.
    val eventThread = event.thread ?: return
    if (eventThread.isFxApplicationThread().not()) {
      return
    }

    val stackTrace = event.stackTrace ?: return
    val frames = stackTrace.frames.map { it.toFrameRef() }

    //Don't let the recording measure itself: the overlay (AllocationRecordingLayer) and currentReport()
    //allocate on the paint thread while rendering. Those allocations carry a Layer.paint frame and
    //would otherwise pollute the report (e.g. as TypeAllocation under the overlay's LayerVisibilityAdapter).
    if (frames.any { it.isRecordingInfrastructure() }) {
      return
    }

    val layerName = extractLayerName(frames) ?: return

    val objectClass = event.getClass("objectClass") ?: return
    val typeName = readableTypeName(objectClass.name)
    val weight = if (event.hasField("weight")) event.getLong("weight") else 0L
    val stacktrace = if (activeMode.capturesStacktrace) formatStacktrace(frames) else null

    recordSample(typeName, weight, layerName, stacktrace)
  }

  /**
   * Aggregates a single sample. Separated from JFR so it can be tested deterministically.
   */
  internal fun recordSample(typeName: String, weight: Long, layerName: String, stacktrace: String?) {
    synchronized(lock) {
      val layerMap = aggregate.getOrPut(layerName) { HashMap() }
      val entry = layerMap.getOrPut(typeName) { MutableTypeAllocation(typeName) }
      entry.samples++
      entry.estimatedBytes += weight
      if (stacktrace != null) {
        entry.stacktraces[stacktrace] = (entry.stacktraces[stacktrace] ?: 0) + 1
      }
    }
  }

  /**
   * Extracts the layer that an allocation is attributed to: the `layout`/`paint` frame of a layer that
   * is closest to the root of the stack. Returns `null` if the allocation did not happen inside a
   * layer's layout or paint (which is how non-render allocations are filtered out).
   *
   * Both phases are counted: `Layers.paintLayers` runs a layout pass and then a paint pass on every
   * frame, and a layer allocates in both (e.g. the history stripe layer boxes its value-class index in
   * layout *and* paint). Recording only `paint` would miss the layout allocations.
   */
  internal fun extractLayerName(frames: List<FrameRef>): String? {
    var layerClassName: String? = null
    frames.forEach { frame ->
      if ((frame.methodName == "paint" || frame.methodName == "layout") && isLikelyLayer(frame.className)) {
        //keep updating - the last match while iterating top -> root is the outermost layout/paint = the layer
        layerClassName = frame.className
      }
    }
    return layerClassName?.substringAfterLast('.')
  }

  private fun isLikelyLayer(className: String): Boolean {
    return className.startsWith("com.meistercharts") && (className.endsWith("Layer") || className.contains(".layers."))
  }

  /**
   * Whether this frame belongs to the allocation recording itself (the engine, its report model, or the
   * overlay layer). Samples with such a frame are the measurement measuring itself and must be dropped.
   */
  internal fun FrameRef.isRecordingInfrastructure(): Boolean {
    return className.startsWith("com.meistercharts.canvas.allocation") || className.endsWith("AllocationRecordingLayer")
  }

  /**
   * Converts a JVM class name as reported by JFR into readable Java source form:
   * `[B` -> `byte[]`, `[[I` -> `int[][]`, `[Ljava.lang.Object;` -> `java.lang.Object[]`. Non-array names
   * are returned unchanged.
   */
  internal fun readableTypeName(name: String): String {
    var dimensions = 0
    while (dimensions < name.length && name[dimensions] == '[') {
      dimensions++
    }
    if (dimensions == 0) {
      return name
    }

    val elementName = when (name[dimensions]) {
      'B' -> "byte"
      'C' -> "char"
      'D' -> "double"
      'F' -> "float"
      'I' -> "int"
      'J' -> "long"
      'S' -> "short"
      'Z' -> "boolean"
      'L' -> name.substring(dimensions + 1, name.length - 1) //strip leading 'L' and trailing ';'
      else -> name.substring(dimensions)
    }
    return elementName + "[]".repeat(dimensions)
  }

  private fun formatStacktrace(frames: List<FrameRef>): String {
    return frames.asSequence()
      .filter { it.className.startsWith("com.meistercharts") || it.className.startsWith("it.neckar") }
      .take(maxStacktraceFrames)
      .joinToString("\n") { "${it.className}.${it.methodName}(${it.lineNumber})" }
  }

  private const val maxStacktraceFrames: Int = 6

  private val logger: Logger = LoggerFactory.getLogger("com.meistercharts.canvas.allocation.AllocationRecordingEngine")
}

/**
 * Whether the (recorded) allocating thread is the JavaFX Application Thread - the paint thread on the
 * JVM. Shares the name check with `Thread.isFxApplicationThread` via [isFxApplicationThreadName], so a
 * JFR [RecordedThread] (which is not a live [Thread]) is recognized the same way.
 */
private fun RecordedThread.isFxApplicationThread(): Boolean {
  return isFxApplicationThreadName(javaName)
}

/**
 * A minimal, testable reference to a stack frame.
 */
internal data class FrameRef(val className: String, val methodName: String, val lineNumber: Int)

private fun RecordedFrame.toFrameRef(): FrameRef {
  val method = this.method
  val className = method?.type?.name ?: "?"
  val methodName = method?.name ?: "?"
  return FrameRef(className, methodName, this.lineNumber)
}

/**
 * Mutable accumulator for the sampled allocations of a single type within one layer.
 */
private class MutableTypeAllocation(val typeName: String) {
  var samples: Int = 0
  var estimatedBytes: Long = 0
  val stacktraces: MutableMap<String, Int> = HashMap()

  fun toTypeAllocation(): TypeAllocation {
    return TypeAllocation(typeName, samples, estimatedBytes, stacktraces.toMap())
  }
}
