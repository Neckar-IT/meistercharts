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

import it.neckar.open.observable.ObservableBoolean

/**
 * Represents a button in the demo.
 * Can be used to modify some properties of the button.
 */
interface DemoToggleButton : DemoButton {
  /**
   * Whether the button is selected.
   * ATTENTION: It is necessary to use a toggle button for this to work!
   */
  val selectedProperty: ObservableBoolean
  var selected: Boolean
}
