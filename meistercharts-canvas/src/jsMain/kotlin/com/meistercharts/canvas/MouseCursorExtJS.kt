package com.meistercharts.canvas


/**
 * Computes the cursor CSS property for this [MouseCursor]
 */
fun MouseCursor.toCss(): String {
  // https://developer.mozilla.org/en-US/docs/Web/CSS/cursor
  return when (this) {
    MouseCursor.Default -> "default"
    MouseCursor.Hand -> "pointer"
    MouseCursor.OpenHand -> "grab"
    MouseCursor.ClosedHand -> "grabbing"
    MouseCursor.CrossHair -> "crosshair"
    MouseCursor.Text -> "text"
    MouseCursor.Busy -> "wait"
    MouseCursor.Move -> "move"
    MouseCursor.None -> "none"
    MouseCursor.ResizeNorth -> "n-resize"
    MouseCursor.ResizeNorthEast -> "ne-resize"
    MouseCursor.ResizeEast -> "e-resize"
    MouseCursor.ResizeSouthEast -> "se-resize"
    MouseCursor.ResizeSouth -> "s-resize"
    MouseCursor.ResizeSouthWest -> "sw-resize"
    MouseCursor.ResizeWest -> "w-resize"
    MouseCursor.ResizeNorthWest -> "nw-resize"
    MouseCursor.ResizeEastWest -> "ew-resize"
    MouseCursor.ResizeNorthSouth -> "ns-resize"
  }
}

/**
 * Contains a list of mouse cursors that are supported by JS but currently not by MeisterCharts
 */
val additionalMouseCursorTypes = listOf(
  "zoom-in",
  "zoom-out",
  "help",
  "context-menu",
  "pointer",
  "progress",
  "cell",
  "not-allowed",
  "all-scroll",
  "col-resize",
  "row-resize",
  "no-drop"
)
