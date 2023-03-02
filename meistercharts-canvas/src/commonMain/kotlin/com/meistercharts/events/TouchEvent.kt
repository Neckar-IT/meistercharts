package com.meistercharts.events

import it.neckar.open.unit.number.PositiveOrZero
import com.meistercharts.annotations.Window
import com.meistercharts.events.ModifierCombination
import com.meistercharts.model.Coordinates
import it.neckar.open.unit.other.px
import it.neckar.open.unit.time.RelativeMillis
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * Sealed base class for a platform-independent touch event.
 *
 * This is closely related to JavaScript touch events (see [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent))
 */
sealed class TouchEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * A list of all the [Touch] objects representing individual points of contact whose states changed between the previous touch event and this one.
   */
  val changedTouches: List<Touch>,

  /**
   * A list of all the [Touch] objects that are both currently in contact with the touch surface and were also started on the same element that is the target of the event.
   */
  val targetTouches: List<Touch>,

  /**
   * A list of all the [Touch] objects representing all current points of contact with the surface, regardless of target or changed status.
   */
  @Deprecated(level = DeprecationLevel.WARNING, message = "In nearly all cases only the target touches are relevant", replaceWith = ReplaceWith("targetTouches"))
  val touches: List<Touch>
) : UiEvent(relativeTimestamp) {


  /**
   * Returns true if the number of:
   * * target touches matches the provided number
   */
  inline fun letIfTouchCount(expectedNumberOfTouches: Int, action: () -> Unit) {
    contract {
      callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    if (this.targetTouchesCount == expectedNumberOfTouches) {
      action()
    }
  }

  /**
   * Executes the action if this is a single touch (on the target)
   */
  inline fun ifSingleTouch(action: () -> Unit) {
    return letIfTouchCount(1, action)
  }

  /**
   * Executes the action if there are *no* touches.
   *
   * This method is only useful for onEnd events
   */
  inline fun ifNoTouch(action: () -> Unit) {
    return letIfTouchCount(0, action)
  }

  /**
   * Executes the action if this is a touch with exactly two fingers (on the target)
   */
  inline fun ifTwoTouches(action: () -> Unit) {
    return letIfTouchCount(2, action)
  }

  /**
   * The number of (total) touches
   */
  @Deprecated(level = DeprecationLevel.WARNING, message = "In nearly all cases only the target touches are relevant", replaceWith = ReplaceWith("targetTouchesCount"))
  val touchesCount: @PositiveOrZero Int
    get() {
      return touches.size
    }

  /**
   * Returns the number of target touches
   */
  val targetTouchesCount: @PositiveOrZero Int
    get() {
      return targetTouches.size
    }

  val changedTouchesCount: @PositiveOrZero Int
    get() {
      return changedTouches.size
    }

  /**
   * The modifier combination that is pressed during the touch event
   */
  abstract val modifierCombination: ModifierCombination

  /**
   * The first changed touch
   */
  val firstChanged: Touch
    get() {
      return changedTouches.first()
    }

  /**
   * The first target touch
   */
  val firstTarget: Touch
    get() {
      return targetTouches.first()
    }
}

/**
 * Sent when the user places a touch point on the touch surface. The event's target will be the element in which the touch occurred.
 *
 * See [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/API/Element/touchstart_event)
 */
class TouchStartEvent(
  relativeTimestamp: @RelativeMillis Double,
  changedTouches: List<Touch>,
  targetTouches: List<Touch>,
  touches: List<Touch>,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : TouchEvent(
  relativeTimestamp,
  changedTouches,
  targetTouches,
  touches
) {
  override fun toString(): String {
    return "Touch Start(changed=$changedTouches, target=$targetTouches, touches=$touches)"
  }
}

/**
 * Sent when the user removes a touch point from the surface (that is, when they lift a finger or stylus from the surface).
 * This is also sent if the touch point moves off the edge of the surface; for example, if the user's finger slides off the edge of the screen.
 * The event's target is the same element that received the touchstart event corresponding to the touch point, even if the touch point has moved outside that element.
 * The touch point (or points) that were removed from the surface can be found in the list specified by the [changedTouches] property.
 *
 * See [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/Events/touchend)
 *
 *
 * Attention:
 * * [touches] does *not* contain the removed touches
 * * [targetTouches] does *not* contain the removed touches
 * * [changedTouches] does (only) contain the removed touches
 */
class TouchEndEvent(
  relativeTimestamp: @RelativeMillis Double,
  /**
   * The removed touches
   */
  changedTouches: List<Touch>,
  /**
   * The *remaining* touches on the device
   */
  targetTouches: List<Touch>,
  /**
   * All *remaining* touches
   */
  touches: List<Touch>,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : TouchEvent(
  relativeTimestamp,
  changedTouches,
  targetTouches,
  touches
) {
  override fun toString(): String {
    return "Touch End(changed=$changedTouches, target=$targetTouches, touches=$touches)"
  }
}

/**
 * Sent when the user moves a touch point along the surface.
 * The event's target is the same element that received the touchstart event corresponding to the touch point, even if the touch point has moved outside that element.
 * This event is also sent if the values of the radius, rotation angle, or force attributes of a touch point change.
 *
 * Note: The rate at which touchmove events is sent is browser-specific, and may also vary depending on the capability of the user's hardware.
 * You must not rely on a specific granularity of these events.
 *
 * See [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/Events/touchmove)
 */
class TouchMoveEvent(
  relativeTimestamp: @RelativeMillis Double,
  changedTouches: List<Touch>,
  targetTouches: List<Touch>,
  touches: List<Touch>,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : TouchEvent(
  relativeTimestamp,
  changedTouches,
  targetTouches,
  touches
) {
  override fun toString(): String {
    return "Touch Move(changed=$changedTouches, target=$targetTouches, touches=$touches)"
  }
}

/**
 * Sent when a touch point has been disrupted in some way.
 * There are several possible reasons why this might happen (and the exact reasons will vary from device to device, as well as browser to browser):
 * * An event of some kind occurred that canceled the touch; this might happen if a modal alert pops up during the interaction.
 * * The touch point has left the document window and moved into the browser's UI area, a plug-in, or other external content.
 * * The user has placed more touch points on the screen than can be supported, in which case the earliest Touch in the TouchList gets canceled.
 *
 * See [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/Events/touchcancel)
 */
class TouchCancelEvent(
  relativeTimestamp: @RelativeMillis Double,
  changedTouches: List<Touch>,
  targetTouches: List<Touch>,
  touches: List<Touch>,
  override val modifierCombination: ModifierCombination = ModifierCombination.None
) : TouchEvent(
  relativeTimestamp,
  changedTouches,
  targetTouches,
  touches
) {
  override fun toString(): String {
    return "Touch Cancel(changed=$changedTouches, target=$targetTouches, touches=$touches)"
  }
}

/**
 * Describes a touch.
 *
 * This is closely related to the JavaScript touch (see [developer.mozilla.org](https://developer.mozilla.org/en-US/docs/Web/API/Touch))
 */
data class Touch(
  /**
   * Returns a unique identifier for this Touch object. A given touch point (say, by a finger) will have the same identifier for the duration of its movement around the surface. This lets you ensure that you're tracking the same touch all the time.
   */
  val touchId: TouchId,

  /**
   * The coordinates of the touch on the canvas
   */
  @Window @px val coordinates: Coordinates
) {
  override fun toString(): String {
    return "Touch(@ $coordinates, id=${touchId.id})"
  }
}

/**
 * A unique identifier for a Touch object.
 */
@JvmInline
value class TouchId(val id: Int)

/**
 * Returns the IDs as string
 */
val List<Touch>.idsToString: String
  get() {
    return this.joinToString { it.touchId.toString() }
  }

