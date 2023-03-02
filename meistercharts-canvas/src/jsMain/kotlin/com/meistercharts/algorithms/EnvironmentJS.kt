package com.meistercharts.algorithms

import kotlinx.browser.window

actual fun extractEnvironment(oldEnvironment: Environment): Environment {
  val multiTouchSupported = window.navigator.maxTouchPoints > 0
  val devicePixelRatio = window.devicePixelRatio

  if (oldEnvironment.multiTouchSupported == multiTouchSupported && oldEnvironment.devicePixelRatio == devicePixelRatio) {
    return oldEnvironment
  }

  return Environment(multiTouchSupported, devicePixelRatio)
}

