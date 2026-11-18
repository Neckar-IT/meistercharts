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
 * What the period strip shows for one window.
 */
data class PeriodStripLayout(
  /** One row per level, coarsest first; each row holds its sections sorted by start. */
  val rows: List<List<ZeitstrahlPocSection>>,
  /** The section at the window center of each level coarser than the rows, coarsest first; a section of 16 tiles and more drops out. */
  val breadcrumbs: List<ZeitstrahlPocSection>,
)
