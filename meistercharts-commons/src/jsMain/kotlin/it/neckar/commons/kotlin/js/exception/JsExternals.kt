package it.neckar.commons.kotlin.js.exception

/**
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/unescape
 *
 * TODO: Should be replaced with  decodeURIComponent() or decodeURI() instead
 */
@Deprecated("Is deprecated!")
external fun unescape(s: String): String
