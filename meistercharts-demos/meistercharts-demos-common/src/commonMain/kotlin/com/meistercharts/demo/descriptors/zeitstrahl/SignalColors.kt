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

import com.meistercharts.color.Color
import com.meistercharts.color.RgbaColor

/**
 * The dark scheme `Signal` of the design: lime periods, cyan persons, red events on near black.
 */
object SignalColors {
  val ground: Color = Color.web("#0A0A0B")
  val ink: Color = Color.web("#F4F4F0")
  val inkSoft: Color = Color.web("#C9C9CC")
  val muted: Color = Color.web("#8E8E96")
  val mutedText: Color = Color.web("#A3A3A8")
  val faint: Color = Color.web("#6A6A72")
  val tickMark: Color = Color.web("#5A5A62")
  val line: Color = Color.web("#232327")
  val gridMajor: Color = Color.web("#26262B")
  val gridMinor: Color = Color.web("#161619")
  val surface: Color = Color.web("#141416")
  val surfaceRaised: Color = Color.web("#2A2A30")
  val failure: Color = Color.web("#FF5A36")

  val period: Color = Color.web("#C6F432")
  val periodMid: Color = Color.web("#8FB02A")
  val periodLow: Color = Color.web("#34421A")
  val person: Color = Color.web("#4CD7F6")
  val personLow: Color = Color.web("#2C8BA0")
  val event: Color = Color.web("#FF5A36")
  val eventLow: Color = Color.web("#A8442C")

  /** The header of an open node on the top level and below it. */
  val openHeaderOuter: Color = Color.web("#3D4A12")
  val openHeaderInner: Color = Color.web("#6E8A1C")

  /** The area of an open node; the frames of nested nodes lie on each other, so each level is stronger. */
  val frameFill: RgbaColor = Color.rgba(198, 244, 50, 0.05)

  fun frameEdge(depth: Int): RgbaColor = Color.rgba(198, 244, 50, if (depth == 1) 0.3 else 0.55)

  val treeLine: RgbaColor = Color.rgba(198, 244, 50, 0.55)
}
