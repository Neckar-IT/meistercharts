package com.meistercharts.font

/**
 * Represents a generic family (fallback font) - as used by the browser
 *
 * See also:
 *  [https://developer.mozilla.org/en-US/docs/Web/CSS/generic-family]
 */
enum class GenericFamily(val keyword: String) {
  Serif("serif"),
  SansSerif("sans-serif"),
  Monospace("monospace"),
  Cursive("cursive"),
  Fantasy("fantasy"),
  SystemUi("system-ui"),
  UiSerif("ui-serif"),
  UiSansSerif("ui-sans-serif"),
  UiMonospace("ui-monospace"),
  UiRounded("ui-rounded"),
  Emoji("emoji"),
  Math("math"),
  Fangsong("fangsong")
}
