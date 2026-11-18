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

/**
 * Assigns the rows of a lane and the indentation of the tree.
 */
object TreeRows {
  /** The indentation of the tree per depth. */
  const val IndentPerDepth: Double = 18.0

  /** The x of a node in the tree. */
  fun nodeLeft(depth: Int): Double = 6.0 + (depth - 1) * IndentPerDepth

  /** The entries of a lane without the parts of a node of the lane: the node stands for them, open or closed. */
  fun withoutPartsOfNodes(entries: List<ZeitstrahlPocEntry>): List<ZeitstrahlPocEntry> {
    val nodeIds = entries.filter { it.isNode }.map { it.id }.toSet()
    return entries.filter { entry -> entry.partOf.none { it in nodeIds } }
  }

  /**
   * Nodes first, the shortest on top, each in a row of its own and an open one followed by the rows of its parts. A long-lived
   * node such as a state fills every window of its time, opens and would push the period the window zoomed into below the lane.
   * The other entries follow per band, most important first, each in the first row of its band [occupied] leaves free.
   */
  fun layout(entries: List<TreeEntry>, occupied: (ZeitstrahlPocEntry) -> ClosedFloatingPointRange<Double>): LaneRows {
    val nodes = mutableListOf<NodeRow>()
    val entriesInRows = mutableListOf<EntryInRow>()
    val bands = mutableListOf<Pair<ZeitstrahlPocBand, Int>>()

    fun layoutLevel(level: List<TreeEntry>, depth: Int, firstRow: Int): Int {
      var nextRow = firstRow
      level.filter { it.entry.isNode }.sortedWith(compareBy({ it.entry.end - it.entry.start }, { it.entry.start })).forEach { node ->
        val row = nextRow++
        if (node.open) {
          nextRow = layoutLevel(node.parts, depth + 1, nextRow)
        }
        nodes.add(NodeRow(node.entry, row, depth, node.open, nextRow - 1))
      }
      val entriesByBand = level.filterNot { it.entry.isNode }.map { it.entry }.groupBy { it.kind.band }
      ZeitstrahlPocBand.entries.filter { entriesByBand.containsKey(it) }.forEach { band ->
        val rows = mutableListOf<EntryRow>()
        entriesByBand.getValue(band).sortedByDescending { it.importance }.forEach { entry ->
          val range = occupied(entry)
          val rowIndex = rows.indexOfFirst { it.isFree(range) }.takeIf { it >= 0 } ?: rows.size.also { rows.add(EntryRow()) }
          rows[rowIndex].occupy(range)
          entriesInRows.add(EntryInRow(entry, nextRow + rowIndex, depth))
        }
        if (depth == 1) {
          bands.add(band to nextRow)
        }
        nextRow += rows.size
      }
      return nextRow
    }

    val rowCount = layoutLevel(entries, 1, 0)
    return LaneRows(rowCount, nodes.sortedBy { it.row }, entriesInRows, bands)
  }

  /**
   * The open nodes whose span holds [seconds], outermost first. Of two open siblings there, the chain whose innermost node is
   * the shortest wins: the period the window zoomed into.
   */
  fun openChainAt(entries: List<TreeEntry>, seconds: Double): List<ZeitstrahlPocEntry> {
    return entries.filter { it.open && it.entry.start <= seconds && seconds <= it.entry.end }
      .map { node -> listOf(node.entry) + openChainAt(node.parts, seconds) }
      .minByOrNull { chain -> chain.last().end - chain.last().start }
      .orEmpty()
  }
}
