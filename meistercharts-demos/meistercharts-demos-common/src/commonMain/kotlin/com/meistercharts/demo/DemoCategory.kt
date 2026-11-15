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
package com.meistercharts.demo

enum class DemoCategory(
  val visibility: Visibility,
) {
  /**
   * Gestalt demos
   */
  Gestalt(Visibility.Public),

  /**
   * Demos for Lizergy
   */
  Lizergy(Visibility.Public),

  /**
   * Demos for BioExP
   */
  Bioexp(Visibility.Public),

  /**
   * Demos for Automation customers
   */
  Automation(Visibility.Public),

  ShowCase(Visibility.Public),

  /**
   * Stuff related to NECKAR.IT
   */
  NeckarIT(Visibility.Public),

  Elektromeister(Visibility.Public),

  /**
   * Contains framework-related demos
   */
  Framework(Visibility.DevOnly),

  /**
   * Support classes that are commonly used
   */
  Support(Visibility.DevOnly),

  /**
   * Layers
   */
  Layers(Visibility.DevOnly),

  Interaction(Visibility.DevOnly),

  /**
   * Axis related stuff
   */
  Axis(Visibility.DevOnly),

  /**
   * Text painting / I18n stuff
   */
  Text(Visibility.DevOnly),

  /**
   * Paintables
   */
  Paintables(Visibility.DevOnly),

  /**
   * Painters that are *not* paintables
   */
  Painters(Visibility.DevOnly),

  /**
   * Primitive drawing operations
   */
  Primitives(Visibility.DevOnly),

  /**
   * Low level tests
   */
  LowLevelTests(Visibility.DevOnly),

  /**
   * All calculations related to content area etc.
   */
  Calculations(Visibility.DevOnly),

  /**
   * Demos that reproduce a (possible/old/existing) bug
   */
  BugHunting(Visibility.DevOnly),

  /**
   * Platform dependent demos
   */
  Platform(Visibility.DevOnly),

  Other(Visibility.DevOnly),
  ;

  enum class Visibility {
    /**
     * The category is only useful for developers - and not visible for "normal" users
     */
    DevOnly,

    /**
     * The category is visible for "normal" users
     */
    Public,
  }
}
