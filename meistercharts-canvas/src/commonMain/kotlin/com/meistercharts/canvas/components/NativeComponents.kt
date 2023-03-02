package com.meistercharts.canvas.components

import it.neckar.open.annotations.Experiment
import com.meistercharts.annotations.Window
import com.meistercharts.model.Coordinates
import it.neckar.open.dispose.Disposable
import it.neckar.open.dispose.DisposeSupport
import it.neckar.open.dispose.OnDispose
import it.neckar.open.observable.ObservableBoolean
import it.neckar.open.observable.ObservableObject

/**
 * Base interface for native components that can be added to a canvas using the [NativeComponentsSupport]
 */
interface NativeComponent : Disposable, OnDispose {
  /**
   * The location of the element
   */
  val locationProperty: ObservableObject<@Window Coordinates>
  var location: @Window Coordinates

  /**
   * The native identifier.
   * Can be used to identify the native object - as required when disposing / moving elements
   */
  var id: Any?
}

/**
 * Base class for native components
 */
abstract class BaseNativeComponent : NativeComponent {
  /**
   * The native identifier.
   * Can be used to identify the native object - as required when disposing / moving elements
   */
  override var id: Any? = null

  final override val locationProperty: ObservableObject<@Window Coordinates> = ObservableObject(Coordinates.origin)
  override var location: @Window Coordinates by locationProperty

  //Stuff related to disposing

  private val disposeSupport = DisposeSupport()

  override fun onDispose(action: () -> Unit) {
    disposeSupport.onDispose(action)
  }

  override fun dispose() {
    disposeSupport.dispose()
  }
}

/**
 * Represents a text input field
 */
@Experiment
class TextInput internal constructor(
) : BaseNativeComponent() {
  /**
   * The text of the text input
   */
  val textProperty: ObservableObject<String> = ObservableObject("")
  var text: String by textProperty
}

@Experiment
class CheckBox internal constructor(
) : BaseNativeComponent() {
  /**
   * The selected state of the check box
   */
  val selectedProperty: ObservableBoolean = ObservableBoolean()
  var selected: Boolean by selectedProperty
}

/**
 * Represents a combo box
 */
@Experiment
class ComboBox internal constructor(
) : BaseNativeComponent() {
  /**
   * The choices of the combo box.
   * Must contain *immutable* lists
   */
  val choicesProperty: ObservableObject<List<Choice>> = ObservableObject(listOf())
  var choices: List<Choice> by choicesProperty

  val selectedProperty: ObservableObject<Choice?> = ObservableObject(null)
  var selected: Choice? by selectedProperty

  /**
   * The converter that is used to format the choice
   */
  val converterProperty: ObservableObject<(Choice) -> String> = ObservableObject<(Choice) -> String> { it?.data.toString() }
  var converter: (Choice) -> String by converterProperty

  /**
   * Represents the null choice
   */
  var nullRepresentation: String = "-"
}

/**
 * Represents one choice within a combo box
 */
data class Choice(
  val data: Any
)
