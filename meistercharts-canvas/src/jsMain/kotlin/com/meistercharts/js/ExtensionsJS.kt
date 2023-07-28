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
package com.meistercharts.js

import org.w3c.dom.Element

/**
 */

/**
 * Removes the element from its parent.
 * Old workaround method - no longer required
 */
@Deprecated("Workaround no longer required", ReplaceWith("this.remove()"))
inline fun Element.removeFromParent() {
  this.remove()
  //parentNode?.removeChild(this) //old implementation that has been used to work around IE11 issue
}
