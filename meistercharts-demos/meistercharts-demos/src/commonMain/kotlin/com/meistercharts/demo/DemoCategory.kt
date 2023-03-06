/**
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
package com.meistercharts.demo

enum class DemoCategory {
  /**
   * Gestalt demos
   */
  Gestalt,

  /**
   * Demos for Lizergy
   */
  Lizergy,

  /**
   * Demos for Automation customers
   */
  Automation,

  ShowCase,

  /**
   * Stuff related to NECKAR.IT
   */
  NeckarIT,

  /**
   * Support classes that are commonly used
   */
  Support,

  /**
   * Layers
   */
  Layers,

  Interaction,

  /**
   * Axis related stuff
   */
  Axis,

  /**
   * Text painting / I18n stuff
   */
  Text,

  /**
   * Paintables
   */
  Paintables,

  /**
   * Painters that are *not* paintables
   */
  Painters,

  /**
   * Primitive drawing operations
   */
  Primitives,

  /**
   * Low level tests
   */
  LowLevelTests,

  /**
   * All calculations related to content area etc.
   */
  Calculations,

  /**
   * Demos that reproduce a (possible/old/existing) bug
   */
  BugHunting,

  /**
   * Platform dependent demos
   */
  Platform,

  Other,
}
