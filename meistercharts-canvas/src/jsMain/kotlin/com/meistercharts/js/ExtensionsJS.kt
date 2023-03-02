package com.meistercharts.js

import org.w3c.dom.Element

/**
 */

/**
 * Removes the element from its parent.
 * This method is IE11 compatible and should be used instead of [Element.remove]
 */
fun Element.removeFromParent() {
  parentNode?.removeChild(this)
}
