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
 * The rows of one lane: each node has a row of its own, an open node is followed by the rows of its parts.
 */
class LaneRows(
  val rowCount: Int,
  val nodes: List<NodeRow>,
  val entries: List<EntryInRow>,
  /** The first row of each band on the top level, the place of its label in the tree. */
  val bands: List<Pair<ZeitstrahlPocBand, Int>>,
)

/** A node and the rows of its frame: [row] holds the node itself, [lastRow] the last row of its parts. */
class NodeRow(
  val entry: ZeitstrahlPocEntry,
  val row: Int,
  /** 1 on the top level of a lane, one more per open node around it. */
  val depth: Int,
  val open: Boolean,
  val lastRow: Int,
)

/** An entry that is no node; it shares [row] with other entries of its band. */
class EntryInRow(
  val entry: ZeitstrahlPocEntry,
  val row: Int,
  val depth: Int,
)
