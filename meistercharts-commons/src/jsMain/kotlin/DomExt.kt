import it.neckar.open.kotlin.lang.fastFor
import kotlinx.browser.document
import org.w3c.dom.COMPLETE
import org.w3c.dom.Document
import org.w3c.dom.DocumentReadyState
import org.w3c.dom.Element
import org.w3c.dom.ItemArrayLike

/**
 * Creates an element and appends it to the parent
 */
fun Element.addElement(localName: String): Element {
  return document.createElement(localName).also {
    this.appendChild(it)
  }
}

/**
 * Iterates over all elements
 */
inline fun <T> ItemArrayLike<T>.forEach(callback: (element: T) -> Unit) {
  length.fastFor { index ->
    val element = item(index)
    if (element != null) {
      callback(element)
    }
  }
}

/**
 * Throws an exception if the ready state of the document is *not* COMPLETE
 */
fun Document.requireComplete() {
  require(this.readyState == DocumentReadyState.COMPLETE) {
    "Expected ready state to be [${DocumentReadyState.COMPLETE}] but was [${this.readyState}]. Maybe `window.onLoad()` should be used."
  }
}
