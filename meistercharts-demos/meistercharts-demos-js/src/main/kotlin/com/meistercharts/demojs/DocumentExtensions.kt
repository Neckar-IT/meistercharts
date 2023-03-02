package com.meistercharts.demojs

import com.meistercharts.algorithms.painter.Color
import it.neckar.open.formatting.NumberFormat
import it.neckar.open.formatting.decimalFormat
import it.neckar.open.observable.ObservableDouble
import it.neckar.open.observable.ObservableObject
import it.neckar.open.observable.ReadOnlyObservableObject
import org.w3c.dom.Document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLOptionElement
import org.w3c.dom.HTMLSelectElement
import org.w3c.dom.HTMLSpanElement
import org.w3c.dom.events.Event
import kotlin.math.pow

/**
 * Creates a H1 - headline with the given [text]
 */
fun Document.headline1(text: String): HTMLElement {
  val h1 = createElement("H1")
  h1.textContent = text
  return h1 as HTMLElement
}

/**
 * Creates a H2 - headline with the given [text]
 */
fun Document.headline2(text: String): HTMLElement {
  val h2 = createElement("H2")
  h2.textContent = text
  return h2 as HTMLElement
}

/**
 * Creates a H3 - headline with the given [text]
 */
fun Document.headline3(text: String): HTMLElement {
  val h3 = createElement("H3")
  h3.textContent = text
  return h3 as HTMLElement
}

/**
 * Creates a label with the given [text]
 */
fun Document.label(text: String): HTMLElement {
  val label = createElement("LABEL")
  label.textContent = text
  label.setAttribute("title", text)
  return label as HTMLElement
}

/**
 * Creates a label whose text matches what [converter] returns for the current value of [text]
 */
fun <T> Document.label(text: ReadOnlyObservableObject<T>, converter: (T) -> String = { it.toString() }): HTMLElement {
  val label = createElement("LABEL")
  text.consume(true) {
    val convertedText = converter(it)
    label.textContent = convertedText
    label.setAttribute("title", convertedText)
  }
  return label as HTMLElement
}

/**
 * Creates a check-box with a label on its right side.
 *
 * The checked state of the check-box is bidirectional bound to [checked].
 */
fun Document.checkBox(checked: ObservableObject<Boolean>, checkBoxLabel: String = ""): HTMLElement {
  val label = createElement("LABEL")

  val checkBox = createElement("INPUT") as HTMLInputElement
  checkBox.setAttribute("type", "checkbox")
  checkBox.style.marginRight = "0.4em"

  checked.consume(true) {
    checkBox.checked = it
  }

  checkBox.addEventListener("change", {
    checked.value = checkBox.checked
  })

  label.appendChild(checkBox)
  checkBox.insertAdjacentText("afterend", checkBoxLabel)
  return label as HTMLElement
}

/**
 * Creates a combo-box whose options are derived from [values].
 *
 * The selection of the combo-box is bidirectional bound to [selected].
 */
fun <E : Enum<E>> Document.comboBox(selected: ObservableObject<E>, values: Array<E>): HTMLElement {
  val comboBox = createElement("SELECT") as HTMLSelectElement
  val options = values.map {
    createElement("OPTION").apply {
      setAttribute("value", it.name)
      textContent = it.name
      comboBox.appendChild(this)
    }
  }.map {
    it as HTMLOptionElement
  }.toList()

  selected.consume(true) { toBeSelected ->
    options.forEach {
      it.selected = it.value == toBeSelected.name
    }
  }

  comboBox.addEventListener("change", {
    values.find {
      comboBox.value == it.name
    }?.let {
      selected.value = it
    }
  })

  return comboBox
}

/**
 * Creates a combo-box whose options are derived from [values].
 *
 * The selection of the combo-box is bidirectional bound to [selected].
 *
 * The given [converter] is used to convert any value of [selected] to a String.
 */
fun <T> Document.comboBox(selected: ObservableObject<T>, values: List<T>, converter: (T) -> String): HTMLElement {
  val comboBox = createElement("SELECT") as HTMLSelectElement
  val options = values.map {
    createElement("OPTION").apply {
      setAttribute("value", converter(it))
      textContent = converter(it)
      comboBox.appendChild(this)
    }
  }.map {
    it as HTMLOptionElement
  }.toList()

  selected.consume(true) { toBeSelected ->
    val targetValue = converter(toBeSelected)
    options.forEach {
      it.selected = it.value == targetValue
    }
  }

  comboBox.addEventListener("change", {
    values.find {
      comboBox.value == converter(it)
    }?.let {
      selected.value = it
    }
  })

  return comboBox
}

/**
 * Creates a stepper with the given [min], [max] and [step] attributes.
 *
 * The value of the stepper is bidirectional bound to [selected].
 */
fun Document.stepper(selected: ObservableDouble, min: Double, max: Double, step: Double): HTMLElement {
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/number
  val number = createElement("INPUT") as HTMLInputElement
  number.setAttribute("type", "number")
  number.setAttribute("min", min.toString())
  number.setAttribute("max", max.toString())
  number.setAttribute("step", step.toString())

  selected.consume(true) {
    number.value = it.toString()
  }

  number.addEventListener("input", {
    number.value.toDoubleOrNull()?.let {
      selected.value = it
    }
  })

  return number
}


/**
 * Returns a span that paints the given property
 */
fun <T> Document.span(property: ObservableObject<T>): HTMLSpanElement {
  val span = createElement("SPAN") as HTMLSpanElement

  property.consumeImmediately {
    span.textContent = it.toString()
  }

  return span
}

/**
 * Creates a slider with the given [min], [max] and [step] attributes.
 *
 * The value of the slider is bidirectional bound to [selected].
 */
fun Document.slider(selected: ObservableObject<Double>, min: Double, max: Double, step: Double): HTMLElement {
  // https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/range

  // compute the number of fraction digits from the value of step
  var fractionDigits = 0
  while (fractionDigits < 6) {
    if (step >= 10.0.pow(-fractionDigits)) {
      break
    }
    ++fractionDigits
  }
  val format: NumberFormat = decimalFormat(fractionDigits, fractionDigits, 1, false)

  val wrapper = createElement("DIV") as HTMLElement
  wrapper.apply {
    style.setProperty("display", "flex")
    style.setProperty("align-items", "center")
    style.setProperty("overflow", "hidden")
  }

  wrapper.appendChild(
    label(format.format(min)).apply {
      style.setProperty("font-size", "smaller")
      style.setProperty("white-space", "nowrap")
    }
  )

  val slider = createElement("INPUT") as HTMLInputElement
  slider.setAttribute("type", "range")
  slider.setAttribute("min", min.toString())
  slider.setAttribute("max", max.toString())
  slider.setAttribute("step", step.toString())
  slider.style.setProperty("width", "100px")

  wrapper.appendChild(slider)

  wrapper.appendChild(
    label(format.format(max)).apply {
      style.setProperty("font-size", "smaller")
      style.setProperty("white-space", "nowrap")
    }
  )

  wrapper.appendChild(
    label(selected) { format.format(it) }.apply {
      style.setProperty("margin-left", "10px")
      style.setProperty("white-space", "nowrap")
    }
  )

  selected.consume(true) {
    slider.value = it.toString()
  }

  //Register callbacks for the slider
  val callback: (Event) -> Unit = {
    slider.value.toDoubleOrNull()?.let {
      selected.value = it
    }
  }
  slider.addEventListener("input", callback) //for modern browsers
  slider.addEventListener("change", callback) //for IE11 - on release

  return wrapper
}

/**
 * Creates a button with the given [text].
 *
 * The [action] is called whenever a user clicks the button.
 */
fun Document.button(text: String, action: () -> Unit): HTMLElement {
  val button = createElement("BUTTON") as HTMLButtonElement
  button.setAttribute("type", "button")
  button.textContent = text

  button.addEventListener("click", {
    action()
  })

  return button
}

/**
 * Creates a color-picker and binds it to [selected].
 *
 * Beware that only opaque colors are supported
 */
fun Document.colorPicker(selected: ObservableObject<Color>): HTMLElement {
  val colorPicker = createElement("INPUT") as HTMLInputElement
  colorPicker.setAttribute("type", "color")
  colorPicker.addEventListener("input", {
    selected.value = Color(colorPicker.value)
  })
  selected.consumeImmediately {
    colorPicker.value = it.web
  }
  return colorPicker
}

/**
 * Creates a table row element
 */
fun Document.tableRow(): HTMLElement {
  return createElement("TR") as HTMLElement
}

/**
 * Creates a table cell element
 */
fun Document.tableCell(): HTMLElement {
  return createElement("TD") as HTMLElement
}
