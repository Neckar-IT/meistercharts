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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class TreeRowsTest {
  private fun entry(id: String, kind: ZeitstrahlPocEntryKind, start: Double, end: Double, childCount: Int = 0, partOf: List<String> = emptyList()): ZeitstrahlPocEntry =
    ZeitstrahlPocEntry(EntryId(id), kind, id, start, end, Importance(0.5), partOf.map { EntryId(it) }, childCount)

  private fun period(id: String, start: Double, end: Double, childCount: Int = 0, partOf: List<String> = emptyList()) = entry(id, ZeitstrahlPocEntryKind.Period, start, end, childCount, partOf)

  private fun event(id: String, at: Double) = entry(id, ZeitstrahlPocEntryKind.Event, at, at + 1.0)

  /** Each entry occupies its span in seconds, so entries far apart share a row. */
  private fun layout(entries: List<TreeEntry>): LaneRows = TreeRows.layout(entries) { it.start..(it.end + 1.0) }

  private fun closed(entry: ZeitstrahlPocEntry) = TreeEntry(entry, false, emptyList())

  @Test
  fun `each node has a row of its own, the shortest on top, while events share a row`() {
    val rows = layout(
      listOf(
        closed(period("Napoleonische Kriege", 1803.0, 1815.0, childCount = 11)),
        closed(period("Spanischer Erbfolgekrieg", 1701.2, 1714.2, childCount = 6)),
        closed(event("Wiener Kongress", 1814.0)),
        closed(event("Reichsgründung", 1871.0)),
      )
    )

    assertThat(rows.nodes.map { it.entry.title to it.row }).containsExactly("Napoleonische Kriege" to 0, "Spanischer Erbfolgekrieg" to 1)
    assertThat(rows.entries.map { it.entry.title to it.row }).containsExactly("Wiener Kongress" to 2, "Reichsgründung" to 2)
    assertThat(rows.rowCount).isEqualTo(3)
  }

  @Test
  fun `an open node is followed by the rows of its parts, one level deeper per node`() {
    val terror = TreeEntry(period("Terrorherrschaft", 1793.4, 1794.6, childCount = 1), true, listOf(closed(event("Hinrichtung Dantons", 1794.3))))
    val revolution = TreeEntry(
      period("Französische Revolution", 1789.3, 1799.9, childCount = 961),
      true,
      listOf(terror, closed(period("Direktorium", 1795.8, 1799.9, childCount = 4)), closed(event("Sturm auf die Bastille", 1789.5))),
    )

    val rows = layout(listOf(revolution, closed(event("Reichsgründung", 1871.0))))

    assertThat(rows.nodes.map { Triple(it.entry.title, it.row, it.depth) }).containsExactly(
      Triple("Französische Revolution", 0, 1),
      Triple("Terrorherrschaft", 1, 2),
      Triple("Direktorium", 3, 2),
    )
    assertThat(rows.nodes.map { it.lastRow }).containsExactly(4, 2, 3)
    assertThat(rows.entries.map { Triple(it.entry.title, it.row, it.depth) }).containsExactly(
      Triple("Hinrichtung Dantons", 2, 3),
      Triple("Sturm auf die Bastille", 4, 2),
      Triple("Reichsgründung", 5, 1),
    )
  }

  @Test
  fun `the tree indents 18 px per level`() {
    assertThat((1..3).map { TreeRows.nodeLeft(it) }).containsExactly(6.0, 24.0, 42.0)
  }

  @Test
  fun `a part of a node of the lane leaves the lane to the node`() {
    val entries = listOf(
      period("Q6534", 0.0, 10.0, childCount = 961),
      period("Q193547", 4.0, 5.0, childCount = 1, partOf = listOf("Q6534")),
      event("Q6539", 1.0),
    )

    assertThat(TreeRows.withoutPartsOfNodes(entries).map { it.id.value }).containsExactly("Q6534", "Q6539")
  }

  @Test
  fun `the path follows the open nodes at the center down to the shortest`() {
    val terror = TreeEntry(period("Terrorherrschaft", 1793.4, 1794.6, childCount = 1), true, emptyList())
    val wars = TreeEntry(period("Französische Revolutionskriege", 1792.3, 1802.2, childCount = 828), true, emptyList())
    val revolution = TreeEntry(period("Französische Revolution", 1789.3, 1799.9, childCount = 961), true, listOf(wars, terror))

    assertThat(TreeRows.openChainAt(listOf(revolution), 1794.0).map { it.title }).containsExactly("Französische Revolution", "Terrorherrschaft")
    assertThat(TreeRows.openChainAt(listOf(revolution), 1796.0).map { it.title }).containsExactly("Französische Revolution", "Französische Revolutionskriege")
  }

  @Test
  fun `a lane gets what it asks for, or an equal share of what the smaller lanes leave`() {
    assertThat(ZeitstrahlPocLayout.laneHeights(listOf(30.0, 400.0, 30.0, 300.0), 500.0)).containsExactly(30.0, 220.0, 30.0, 220.0)
  }
}
