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
package com.meistercharts.demo.descriptors.zeitstrahl

import com.meistercharts.algorithms.layers.AbstractLayer
import com.meistercharts.algorithms.layers.LayerPaintingContext
import com.meistercharts.algorithms.layers.LayerType
import com.meistercharts.canvas.CanvasRenderingContext
import com.meistercharts.canvas.ChartSupport
import com.meistercharts.canvas.DirtyReason
import com.meistercharts.canvas.events.CanvasMouseEventHandler
import com.meistercharts.canvas.fill
import com.meistercharts.canvas.fillRoundedRect
import com.meistercharts.canvas.saved
import com.meistercharts.canvas.strokeRoundedRect
import com.meistercharts.canvas.strokeStyle
import com.meistercharts.color.Color
import com.meistercharts.events.EventConsumption
import com.meistercharts.font.FontDescriptorFragment
import com.meistercharts.font.FontSize
import com.meistercharts.font.FontWeight
import it.neckar.events.MouseDownEvent
import it.neckar.geometry.Direction
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**
 * One lane per node of the category tree, each with the tree of its periods with parts in the column `STRUKTUR` on the left.
 *
 * A lane loads the entries visible on the current zoom level. A period with parts is a node with a row of its own; an open node
 * loads its parts in the window and shows them in a frame below its row, at every depth by the same rules. The other entries
 * share rows per band, most important first; an entry beyond the height of its lane stays hidden and counts in the lane title.
 */
class ZeitstrahlPocLanesLayer(
  private val viewport: DeepTimeViewport,
  private val rowScale: RowScale,
  lanes: List<ZeitstrahlPocLane>,
  createLaneLoader: (ZeitstrahlPocLane) -> WindowLoader<ZoomLevel, ZeitstrahlPocEntry>,
  private val createPartsLoader: () -> WindowLoader<EntryId, ZeitstrahlPocEntry>,
) : AbstractLayer() {
  override val type: LayerType = LayerType.Content

  private val lanesWithLoaders: List<Pair<ZeitstrahlPocLane, WindowLoader<ZoomLevel, ZeitstrahlPocEntry>>> = lanes.map { it to createLaneLoader(it) }

  private val decisions: OpenDecisions = OpenDecisions()

  /** The loader of the parts of each open node. */
  private val partsLoaders: MutableMap<EntryId, WindowLoader<EntryId, ZeitstrahlPocEntry>> = mutableMapOf()

  /** The tree rows of the nodes of the last paint, for a click. */
  private val nodeHits: MutableList<NodeHit> = mutableListOf()

  override fun paint(paintingContext: LayerPaintingContext) {
    val gc = paintingContext.gc
    if (viewport.windowWidth <= 0.0) {
      return
    }
    val rowHeight = rowScale.rowHeight

    val openNodes = mutableSetOf<EntryId>()
    val trees = lanesWithLoaders.map { (lane, loader) ->
      loader.ensureLoaded(viewport.zoomLevel, viewport.visibleFrom, viewport.visibleTo)
      val visible = loader.items.filter { viewport.overlaps(it.start, it.end) }
      LaneTree(lane, loader.failure, visible.size, buildLevel(TreeRows.withoutPartsOfNodes(visible), 1, openNodes))
    }
    decisions.endPass()
    partsLoaders.keys.retainAll(openNodes)

    val layouts = trees.map { tree -> TreeRows.layout(tree.entries) { entry -> occupiedRange(gc, entry, rowHeight) } }
    val heights = ZeitstrahlPocLayout.laneHeights(
      layouts.map { ZeitstrahlPocLayout.LaneTitleHeight + it.rowCount * rowHeight + ZeitstrahlPocLayout.LaneBottomPadding },
      gc.height - ZeitstrahlPocLayout.StripHeight - ZeitstrahlPocLayout.LanesTop,
    )

    nodeHits.clear()
    var laneTop = ZeitstrahlPocLayout.LanesTop
    trees.forEachIndexed { index, tree ->
      paintLane(gc, tree, layouts[index], laneTop, heights[index], rowHeight)
      laneTop += heights[index]
    }
    gc.strokeStyle(SignalColors.line)
    gc.lineWidth = 1.0
    gc.strokeLine(0.0, laneTop, gc.width, laneTop)

    paintPath(gc, trees)
  }

  private fun buildLevel(entries: List<ZeitstrahlPocEntry>, depth: Int, openNodes: MutableSet<EntryId>): List<TreeEntry> = entries.map { entry ->
    // A cycle in `partOf` would open the same node again and again.
    val open = entry.isNode && decisions.update(entry.id, shareOf(entry)) && depth < MaxDepth
    if (!open) {
      return@map TreeEntry(entry, false, emptyList())
    }
    openNodes.add(entry.id)
    val loader = partsLoaders.getOrPut(entry.id) { createPartsLoader() }
    loader.ensureLoaded(entry.id, viewport.visibleFrom, viewport.visibleTo)
    val parts = if (loader.loadedKey == entry.id) loader.items.filter { viewport.overlaps(it.start, it.end) } else emptyList()
    TreeEntry(entry, true, buildLevel(parts, depth + 1, openNodes))
  }

  /** The part of the window width the span of [entry] covers. */
  private fun shareOf(entry: ZeitstrahlPocEntry): Double {
    val visibleX = viewport.visibleXRange(entry.start, entry.end)
    return ((visibleX.endInclusive - visibleX.start) / viewport.windowWidth).coerceIn(0.0, 1.0)
  }

  private fun paintLane(gc: CanvasRenderingContext, tree: LaneTree, rows: LaneRows, laneTop: Double, laneHeight: Double, rowHeight: Double) {
    val rowsTop = laneTop + ZeitstrahlPocLayout.LaneTitleHeight
    val visibleRows = floor((laneHeight - ZeitstrahlPocLayout.LaneTitleHeight) / rowHeight).toInt().coerceAtLeast(0)
    val drawn = rows.nodes.count { it.row < visibleRows } + rows.entries.count { it.row < visibleRows }
    fun rowTop(row: Int): Double = rowsTop + row * rowHeight

    gc.strokeStyle(SignalColors.line)
    gc.lineWidth = 1.0
    gc.strokeLine(0.0, laneTop, gc.width, laneTop)
    val titleCenterY = laneTop + ZeitstrahlPocLayout.LaneTitleHeight / 2
    gc.font(FontDescriptorFragment(size = FontSize(13.0), weight = FontWeight.Bold))
    gc.fill(SignalColors.ink)
    val label = tree.lane.label.uppercase()
    gc.fillText(label, 14.0, titleCenterY, Direction.CenterLeft)
    val countX = 14.0 + gc.calculateTextWidth(label) + 12.0
    gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Normal))
    gc.fill(SignalColors.muted)
    gc.fillText("$drawn/${tree.visibleCount}", countX, titleCenterY, Direction.CenterLeft)
    tree.failure?.let { failure ->
      gc.fill(SignalColors.failure)
      gc.fillText(serverFailureLabel(failure), ZeitstrahlPocLayout.TreeWidth + 8.0, titleCenterY, Direction.CenterLeft)
    }

    gc.saved {
      gc.clip(0.0, rowsTop, gc.width, laneHeight - ZeitstrahlPocLayout.LaneTitleHeight)

      rows.nodes.filter { it.open }.forEach { node -> paintFrame(gc, node, rowTop(node.row), rowTop(node.lastRow + 1)) }
      paintTreeLines(gc, rows, rowHeight, ::rowTop)

      gc.font(FontDescriptorFragment(size = FontSize(10.5), weight = FontWeight.Normal))
      gc.fill(SignalColors.faint)
      rows.bands.forEach { (band, row) -> gc.fillText(band.label, 14.0, rowTop(row) + rowHeight / 2, Direction.CenterLeft) }
      rows.nodes.forEach { node ->
        paintTreeNode(gc, node, rowTop(node.row), rowHeight)
        if (node.row < visibleRows) {
          nodeHits.add(NodeHit(node.entry.id, TreeRows.nodeLeft(node.depth), rowTop(node.row), rowTop(node.row) + rowHeight))
        }
      }

      gc.saved {
        gc.translate(ZeitstrahlPocLayout.TreeWidth, 0.0)
        gc.clip(0.0, rowsTop, viewport.windowWidth, laneHeight - ZeitstrahlPocLayout.LaneTitleHeight)
        rows.nodes.forEach { node -> paintNodeBar(gc, node, rowTop(node.row), rowHeight) }
        rows.entries.forEach { entryInRow ->
          val entry = entryInRow.entry
          when (entry.kind.band) {
            ZeitstrahlPocBand.Periods -> paintPeriodBar(gc, entry, rowTop(entryInRow.row), rowHeight, periodColors(entry), chip = null, sticky = false)
            ZeitstrahlPocBand.Persons -> paintPerson(gc, entry, rowTop(entryInRow.row), rowHeight)
            ZeitstrahlPocBand.Events -> paintEvent(gc, entry, rowTop(entryInRow.row), rowHeight)
          }
        }
      }
    }
  }

  /** The bracket of an open node in the tree and its frame over the span in the time axis, both over every row of the frame. */
  private fun paintFrame(gc: CanvasRenderingContext, node: NodeRow, top: Double, bottom: Double) {
    val fill = SignalColors.frameFill
    val treeLeft = TreeRows.nodeLeft(node.depth) - 2.0
    gc.fill(fill)
    gc.fillRoundedRect(treeLeft, top, ZeitstrahlPocLayout.TreeWidth - treeLeft, bottom - top, 8.0, 0.0, 0.0, 8.0)

    val startX = viewport.xOf(node.entry.start)
    val endX = viewport.xOf(node.entry.end)
    val continuesLeft = startX < 0.0
    val continuesRight = endX > viewport.windowWidth
    val left = ZeitstrahlPocLayout.TreeWidth + max(startX, 0.0)
    val right = ZeitstrahlPocLayout.TreeWidth + min(endX, viewport.windowWidth)
    val leftRadius = if (continuesLeft) 0.0 else FrameRadius
    val rightRadius = if (continuesRight) 0.0 else FrameRadius
    gc.fillRoundedRect(left, top, right - left, bottom - top, leftRadius, rightRadius, rightRadius, leftRadius)
    gc.strokeStyle(SignalColors.frameEdge(node.depth))
    gc.lineWidth = 1.5
    if (!continuesLeft) {
      gc.strokeLine(left, top + FrameRadius, left, bottom - FrameRadius)
    }
    if (!continuesRight) {
      gc.strokeLine(right, top + FrameRadius, right, bottom - FrameRadius)
    }
  }

  /** A vertical line below each open node down to its last child node, a horizontal line from there to each child node. */
  private fun paintTreeLines(gc: CanvasRenderingContext, rows: LaneRows, rowHeight: Double, rowTop: (Int) -> Double) {
    gc.strokeStyle(SignalColors.treeLine)
    gc.lineWidth = 1.5
    rows.nodes.filter { it.open }.forEach { parent ->
      val lineX = TreeRows.nodeLeft(parent.depth) + 8.0
      val children = rows.nodes.filter { it.depth == parent.depth + 1 && it.row > parent.row && it.row <= parent.lastRow }
      val bottom = children.maxOfOrNull { rowTop(it.row) + rowHeight / 2 } ?: (rowTop(parent.row) + rowHeight * 1.5)
      gc.strokeLine(lineX, rowTop(parent.row) + rowHeight - 3.0, lineX, bottom)
      children.forEach { child ->
        val centerY = rowTop(child.row) + rowHeight / 2
        gc.strokeLine(lineX, centerY, TreeRows.nodeLeft(child.depth), centerY)
      }
    }
  }

  /** Arrow, name and the number of entries below a closed node. */
  private fun paintTreeNode(gc: CanvasRenderingContext, node: NodeRow, rowTop: Double, rowHeight: Double) {
    val left = TreeRows.nodeLeft(node.depth)
    val centerY = rowTop + rowHeight / 2
    val color = if (node.open) SignalColors.period else SignalColors.ink
    gc.fill(color)
    if (node.open) {
      gc.fillTriangle(left + 4.0, centerY - 3.0, left + 12.0, centerY - 3.0, left + 8.0, centerY + 3.0)
    } else {
      gc.fillTriangle(left + 5.0, centerY - 4.0, left + 11.0, centerY, left + 5.0, centerY + 4.0)
    }
    val count = if (node.open) "" else "+${node.entry.childCount}"
    gc.font(FontDescriptorFragment(size = FontSize(10.5), weight = FontWeight.Bold))
    val countWidth = if (count.isEmpty()) 0.0 else gc.calculateTextWidth(count) + 6.0
    gc.font(FontDescriptorFragment(size = FontSize(fontSize(13.0, rowHeight)), weight = if (node.open) FontWeight.Bold else FontWeight.Normal))
    val labelX = left + 18.0
    val labelWidth = min(gc.calculateTextWidth(node.entry.title), ZeitstrahlPocLayout.TreeWidth - 6.0 - labelX - countWidth)
    gc.fillText(node.entry.title, labelX, centerY, Direction.CenterLeft, maxWidth = labelWidth)
    if (count.isNotEmpty()) {
      gc.font(FontDescriptorFragment(size = FontSize(10.5), weight = FontWeight.Bold))
      gc.fill(SignalColors.muted)
      gc.fillText(count, labelX + labelWidth + 6.0, centerY, Direction.CenterLeft)
    }
  }

  /** The bar of a node: the header of its frame while open, its label stays at the left edge of the window. */
  private fun paintNodeBar(gc: CanvasRenderingContext, node: NodeRow, rowTop: Double, rowHeight: Double) {
    if (node.open) {
      val colors = if (node.depth == 1) BarColors(SignalColors.openHeaderOuter, SignalColors.period) else BarColors(SignalColors.openHeaderInner, SignalColors.ground)
      paintPeriodBar(gc, node.entry, rowTop, rowHeight, colors, chip = null, sticky = true)
    } else {
      paintPeriodBar(gc, node.entry, rowTop, rowHeight, periodColors(node.entry), chip = "+${node.entry.childCount}", sticky = false)
    }
  }

  private fun periodColors(entry: ZeitstrahlPocEntry): BarColors = when {
    entry.importance.value >= 0.8 -> BarColors(SignalColors.period, SignalColors.ground)
    entry.importance.value >= 0.5 -> BarColors(SignalColors.periodMid, SignalColors.ground)
    else -> BarColors(SignalColors.periodLow, SignalColors.period)
  }

  private fun paintPeriodBar(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, rowTop: Double, rowHeight: Double, colors: BarColors, chip: String?, sticky: Boolean) {
    val bar = barOf(entry)
    val height = rowHeight - 4.0
    val radius = height / 2
    val leftRadius = if (bar.continuesLeft) 0.0 else radius
    val rightRadius = if (bar.continuesRight) 0.0 else radius
    gc.fill(colors.background)
    gc.fillRoundedRect(bar.left, rowTop + 2.0, bar.right - bar.left, height, leftRadius, rightRadius, rightRadius, leftRadius)

    val label = barLabel(gc, entry, bar, rowHeight, chip, sticky)
    val centerY = rowTop + rowHeight / 2
    gc.fill(if (label.inside) colors.text else SignalColors.ink)
    gc.fillText(label.text, label.x, centerY, Direction.CenterLeft, maxWidth = label.textWidth)
    if (chip != null) {
      paintChip(gc, chip, label.x + label.textWidth + 8.0, centerY, outlined = !label.inside)
    }
  }

  /** "+14" behind the label of a closed node. */
  private fun paintChip(gc: CanvasRenderingContext, text: String, left: Double, centerY: Double, outlined: Boolean) {
    gc.font(ChipFont)
    val width = gc.calculateTextWidth(text) + 14.0
    if (outlined) {
      gc.strokeStyle(SignalColors.period)
      gc.lineWidth = 1.5
      gc.strokeRoundedRect(left, centerY - 7.5, width, 15.0, 7.5)
    } else {
      gc.fill(SignalColors.ground)
      gc.fillRoundedRect(left, centerY - 7.5, width, 15.0, 7.5)
    }
    gc.fill(SignalColors.period)
    gc.fillText(text, left + 7.0, centerY, Direction.CenterLeft)
  }

  private fun barOf(entry: ZeitstrahlPocEntry): Bar {
    val startX = viewport.xOf(entry.start)
    val endX = viewport.xOf(entry.end)
    val left = max(startX, 0.0)
    return Bar(left, max(min(endX, viewport.windowWidth), left + MinBarWidth), startX < 0.0, endX > viewport.windowWidth)
  }

  /** The label goes into the bar if it fits there, else right of the bar, or left of it while the bar runs over the right edge. */
  private fun barLabel(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, bar: Bar, rowHeight: Double, chip: String?, sticky: Boolean): BarLabel {
    val text = (if (bar.continuesLeft) "◂ " else "") + entry.title + (if (bar.continuesRight) " ▸" else "")
    gc.font(ChipFont)
    val chipWidth = chip?.let { gc.calculateTextWidth(it) + 22.0 } ?: 0.0
    gc.font(FontDescriptorFragment(size = FontSize(fontSize(12.5, rowHeight)), weight = FontWeight.Bold))
    val textWidth = min(gc.calculateTextWidth(text), MaxLabelWidth)
    val barWidth = bar.right - bar.left
    return when {
      sticky -> BarLabel(text, true, bar.left + 10.0, min(textWidth, max(barWidth - 20.0, 0.0)))
      textWidth + 20.0 + chipWidth <= barWidth -> BarLabel(text, true, bar.left + 10.0, textWidth)
      bar.continuesRight -> BarLabel(text, false, bar.left - 8.0 - textWidth - chipWidth, textWidth)
      else -> BarLabel(text, false, bar.right + 8.0, textWidth)
    }
  }

  private fun paintPerson(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, rowTop: Double, rowHeight: Double) {
    val startX = viewport.xOf(entry.start)
    val endX = viewport.xOf(entry.end)
    val lineY = rowTop + rowHeight - 5.0
    val color = if (entry.importance.value >= 0.8) SignalColors.person else SignalColors.personLow
    val lineStart = max(startX, -2.0)
    gc.fill(color)
    gc.fillRect(lineStart, lineY - 1.0, max(min(endX, viewport.windowWidth + 2.0) - lineStart, 2.0), 2.0)
    if (startX >= 0.0) {
      gc.fillOvalCenter(startX, lineY, 8.0)
    }
    if (endX <= viewport.windowWidth) {
      gc.fill(SignalColors.ground)
      gc.fillOvalCenter(endX, lineY, 8.0)
      gc.strokeStyle(color)
      gc.lineWidth = 2.0
      gc.strokeOvalCenter(endX, lineY, 6.0)
    }
    val label = personLabel(gc, entry, rowHeight)
    gc.font(personFont(entry, rowHeight))
    gc.fill(SignalColors.ink)
    gc.fillText(label.text, label.x, lineY - 4.0, Direction.BottomLeft, maxWidth = label.textWidth)
    gc.font(FontDescriptorFragment(size = FontSize(fontSize(10.5, rowHeight)), weight = FontWeight.Normal))
    gc.fill(SignalColors.muted)
    gc.fillText(lifeYears(entry), label.x + label.textWidth + 8.0, lineY - 4.0, Direction.BottomLeft)
  }

  private fun personLabel(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, rowHeight: Double): BarLabel {
    val startX = viewport.xOf(entry.start)
    val text = continuedLabel(entry.title, startX)
    gc.font(personFont(entry, rowHeight))
    return BarLabel(text, false, max(startX, 0.0) + if (startX < 0.0) 6.0 else 8.0, min(gc.calculateTextWidth(text), MaxLabelWidth))
  }

  private fun personFont(entry: ZeitstrahlPocEntry, rowHeight: Double): FontDescriptorFragment =
    FontDescriptorFragment(size = FontSize(fontSize(13.0, rowHeight - 2.0)), weight = if (entry.importance.value >= 0.8) FontWeight.Bold else FontWeight.Normal)

  /** "1758–1794" */
  private fun lifeYears(entry: ZeitstrahlPocEntry): String {
    val birth = ProlepticGregorianCalendar.civilDate(entry.start).year
    val death = ProlepticGregorianCalendar.civilDate(entry.end).year
    return "${DeepTimeFormat.historicalYear(birth)}–${DeepTimeFormat.historicalYear(death)}"
  }

  private fun paintEvent(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, rowTop: Double, rowHeight: Double) {
    val x = viewport.xOf(entry.start)
    val size = eventSize(entry, rowHeight)
    val centerY = rowTop + rowHeight / 2
    val important = entry.importance.value >= 0.5
    gc.fill(if (important) SignalColors.event else SignalColors.eventLow)
    gc.fillOvalCenter(x, centerY, size)
    gc.font(eventFont(entry, rowHeight))
    gc.fill(if (important) SignalColors.ink else SignalColors.mutedText)
    gc.fillText(entry.title, x + size / 2 + 6.0, centerY, Direction.CenterLeft, maxWidth = MaxLabelWidth)
  }

  private fun eventSize(entry: ZeitstrahlPocEntry, rowHeight: Double): Double = min(7.0 + 7.0 * entry.importance.value, rowHeight - 6.0)

  private fun eventFont(entry: ZeitstrahlPocEntry, rowHeight: Double): FontDescriptorFragment =
    FontDescriptorFragment(size = FontSize(fontSize(11.5 + 2.0 * entry.importance.value, rowHeight)), weight = if (entry.importance.value >= 0.8) FontWeight.Bold else FontWeight.Normal)

  /** The range a row keeps free for [entry] and its label; nodes have rows of their own. */
  private fun occupiedRange(gc: CanvasRenderingContext, entry: ZeitstrahlPocEntry, rowHeight: Double): ClosedFloatingPointRange<Double> = when (entry.kind.band) {
    ZeitstrahlPocBand.Periods -> {
      val bar = barOf(entry)
      val label = barLabel(gc, entry, bar, rowHeight, chip = null, sticky = false)
      min(bar.left, label.x)..(max(bar.right, label.x + label.textWidth) + LabelGap)
    }

    ZeitstrahlPocBand.Persons -> {
      val startX = viewport.xOf(entry.start)
      val label = personLabel(gc, entry, rowHeight)
      gc.font(FontDescriptorFragment(size = FontSize(fontSize(10.5, rowHeight)), weight = FontWeight.Normal))
      val labelEnd = label.x + label.textWidth + 8.0 + gc.calculateTextWidth(lifeYears(entry))
      (max(startX, 0.0) - 4.0)..(max(min(viewport.xOf(entry.end), viewport.windowWidth) + 4.0, labelEnd) + LabelGap)
    }

    ZeitstrahlPocBand.Events -> {
      val x = viewport.xOf(entry.start)
      val size = eventSize(entry, rowHeight)
      gc.font(eventFont(entry, rowHeight))
      (x - size / 2)..(x + size / 2 + 6.0 + min(gc.calculateTextWidth(entry.title), MaxLabelWidth) + LabelGap)
    }
  }

  /**
   * The open chain at the window center: "Politik / Französische Revolution / Terrorherrschaft".
   * Of several lanes the one whose innermost open node is the shortest wins, the period the user zoomed into; of two lanes
   * with the same node, the lane of the deeper category, as `Politik` before `Fauna`.
   */
  private fun paintPath(gc: CanvasRenderingContext, trees: List<LaneTree>) {
    val (tree, chain) = trees.map { it to TreeRows.openChainAt(it.entries, viewport.centerSeconds) }
      .filter { it.second.isNotEmpty() }
      .minWithOrNull(compareBy<Pair<LaneTree, List<ZeitstrahlPocEntry>>> { (_, chain) -> chain.last().end - chain.last().start }.thenByDescending { (tree, _) -> tree.lane.node.depth })
      ?: return
    val centerY = ZeitstrahlPocLayout.PathHeight / 2
    var x = 14.0
    val segments = listOf(tree.lane.label) + chain.map { it.title }
    segments.forEachIndexed { index, segment ->
      val last = index == segments.lastIndex
      gc.font(FontDescriptorFragment(size = FontSize(17.0), weight = FontWeight.Bold))
      gc.fill(if (last) SignalColors.period else SignalColors.inkSoft)
      gc.fillText(segment, x, centerY, Direction.CenterLeft)
      x += gc.calculateTextWidth(segment) + 10.0
      if (!last) {
        gc.font(FontDescriptorFragment(size = FontSize(17.0), weight = FontWeight.Normal))
        gc.fill(SignalColors.tickMark)
        gc.fillText("/", x, centerY, Direction.CenterLeft)
        x += gc.calculateTextWidth("/") + 10.0
      }
    }
    partsLoaders.values.firstNotNullOfOrNull { it.failure }?.let { failure ->
      gc.font(FontDescriptorFragment(size = FontSize(11.0), weight = FontWeight.Normal))
      gc.fill(SignalColors.failure)
      gc.fillText(serverFailureLabel(failure), x + 10.0, centerY, Direction.CenterLeft)
    }
  }

  override val mouseEventHandler: CanvasMouseEventHandler = object : CanvasMouseEventHandler {
    override fun onDown(event: MouseDownEvent, chartSupport: ChartSupport): EventConsumption {
      val hit = nodeHits.firstOrNull { it.contains(event.x, event.y) } ?: return EventConsumption.Ignored
      decisions.toggle(hit.id)
      chartSupport.markAsDirty(DirtyReason.UserInteraction)
      return EventConsumption.Consumed
    }
  }

  private class LaneTree(val lane: ZeitstrahlPocLane, val failure: String?, val visibleCount: Int, val entries: List<TreeEntry>)

  private class NodeHit(val id: EntryId, val left: Double, val top: Double, val bottom: Double) {
    fun contains(x: Double, y: Double): Boolean = x >= left && x <= ZeitstrahlPocLayout.TreeWidth && y >= top && y < bottom
  }

  /** The visible part of a span in window pixels; [right] keeps [MinBarWidth] after [left]. */
  private class Bar(val left: Double, val right: Double, val continuesLeft: Boolean, val continuesRight: Boolean)

  private class BarLabel(val text: String, val inside: Boolean, val x: Double, val textWidth: Double)

  private class BarColors(val background: Color, val text: Color)

  private companion object {
    const val MinBarWidth: Double = 6.0
    const val MaxLabelWidth: Double = 220.0
    const val LabelGap: Double = 8.0
    const val FrameRadius: Double = 12.0

    /** `partOf` holds at most 10 ancestors; a deeper chain is a cycle. */
    const val MaxDepth: Int = 11

    val ChipFont: FontDescriptorFragment = FontDescriptorFragment(size = FontSize(10.5), weight = FontWeight.Bold)

    /** [design] px, at most what a row of [rowHeight] px leaves free. */
    fun fontSize(design: Double, rowHeight: Double): Double = min(design, rowHeight - 8.0).coerceAtLeast(8.0)
  }
}
