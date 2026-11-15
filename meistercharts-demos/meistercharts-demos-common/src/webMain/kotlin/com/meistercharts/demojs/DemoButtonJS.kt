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
package com.meistercharts.demojs

import com.meistercharts.demo.DemoButton
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableString
import org.w3c.dom.HTMLButtonElement

/**
 *
 */
open class DemoButtonJS(val button: HTMLButtonElement) : DemoButton {
  override val textProperty: ObservableString = ObservableString(button.textContent ?: "-").also {
    it.consume { updatedValue ->
      button.textContent = updatedValue
    }
  }

  override var text: String by textProperty

  override val disabledProperty: ObservableBoolean = ObservableBoolean(button.disabled).also {
    it.consume { updatedValue ->
      button.disabled = updatedValue
    }
  }

  override var disabled: Boolean by disabledProperty
}
