package it.neckar.commons.kotlin.js

import it.neckar.open.collections.fastForEach
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.parsing.XMLSerializer


/**
 * Renders a document to a string
 */
fun Document.render(): String {
  return XMLSerializer().serializeToString(this)
}

/**
 * Removes all elements
 */
fun HTMLElement.removeAll(elementsToRemove: List<HTMLElement>) {
  elementsToRemove.fastForEach {
    removeChild(it)
  }
}

fun HTMLElement.appendAll(addedElements: List<HTMLElement>) {
  addedElements.fastForEach {
    this.appendChild(it)
  }
}

/**
 * Returns the first parent that contains the provided class
 */
fun HTMLElement.findParentWithClass(className: String): Element? {
  var parent = this.parentElement
  while (parent != null) {
    if (parent.classList.contains(className)) {
      return parent
    }
    parent = parent.parentElement
  }
  return null
}

