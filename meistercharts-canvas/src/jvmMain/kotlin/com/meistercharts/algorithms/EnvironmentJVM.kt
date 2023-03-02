package com.meistercharts.algorithms

actual fun extractEnvironment(oldEnvironment: Environment): Environment {
  if (oldEnvironment.devicePixelRatio == mainScreenDevicePixelRatio) {
    return oldEnvironment
  }

  // TODO detect whether multi-touch is supported
  return Environment(false, mainScreenDevicePixelRatio)
}

/**
 * The device pixel ratio for the main screen
 */
var mainScreenDevicePixelRatio: Double = 1.0
