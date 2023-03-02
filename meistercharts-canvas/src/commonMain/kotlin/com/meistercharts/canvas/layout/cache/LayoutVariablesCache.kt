package com.meistercharts.canvas.layout.cache

/**
 * Tagging interface to identify classes that should only be used in layout [com.meistercharts.algorithms.layers.Layer.layout] related circumstances to avoid object instantiation.
 *
 * Usage of this class is *NOT* thread safe!
 */
interface LayoutVariablesCache : LayoutVariable {
}
