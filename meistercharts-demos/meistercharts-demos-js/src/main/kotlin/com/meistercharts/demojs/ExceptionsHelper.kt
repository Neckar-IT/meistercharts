package com.meistercharts.demojs

/**
 * throws different kind of exceptions
 */
@JsExport
@JsName("throwIllArgumentException")
fun throwIllArgumentException() {
  throw IllegalArgumentException("The given string is not valid")
}

@JsExport
@JsName("throwError")
fun throwError() {
  throw Error("This is the error message")
}
