package com.meistercharts.canvas.layout.cache

import org.junit.jupiter.api.Test


class LayoutVariablesObjectCacheTest {
  @Test
  fun testIt() {
    val cache = LayoutVariablesObjectCache { MyLayoutVars() }

    cache.prepare(3)
    cache[0].x = 17.0
    cache[1].x = 18.0
    cache[2].x = 19.0
  }

  private class MyLayoutVars : LayoutVariable {
    var x: Double = Double.NaN

    override fun reset() {
      x = Double.NaN
    }
  }
}

