/**
 * Copyright 2023 Neckar IT GmbH, Mössingen, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.meistercharts.design

import com.meistercharts.color.ColorMapperNullable
import com.meistercharts.color.ColorProvider
import com.meistercharts.color.ColorProviderNullable
import it.neckar.open.provider.MultiProvider

/**
 * A key that represents a style or value associated with a theme
 *
 * @param T the type of the value or style associated with this theme key.
 * @property id the unique identifier of the theme key.
 * @property defaultStyleProvider a function that returns the default style or value associated with this theme key for the given [Theme].
 */
data class ThemeKey<T>(val id: String, val defaultStyleProvider: (Theme) -> T) {
  /**
   * Returns the value for the theme
   */
  @Deprecated("Use provider() instead to support theme changes - in most cases!", ReplaceWith("provider()"))
  fun resolve(design: Theme = CurrentTheme): T {
    return design.resolve(this)
  }

  /**
   * Returns the value as provider
   */
  fun provider(): () -> T {
    @Suppress("DEPRECATION")
    return { resolve() }
  }

  /**
   * Returns a [MultiProvider] that always returns the resolved value for this theme key
   */
  fun <IndexContext> multiProviderAlways(): MultiProvider<IndexContext, T> {
    val multiProvider: MultiProvider<IndexContext, T> = MultiProvider {
      @Suppress("DEPRECATION")
      resolve()
    }
    return multiProvider
  }
}

/**
 * Returns a provider for the theme key
 */
fun <IndexContext, T> ThemeKey<MultiProvider<IndexContext, T>>.multiProvider(): MultiProvider<IndexContext, T> {
  return MultiProvider.delegating {
    @Suppress("DEPRECATION")
    resolve()
  }
}

/**
 * Creates a color mapper resolved later
 */
fun ThemeKey<ColorMapperNullable>.colorMapperProvider(): ColorMapperNullable {
  return { color ->
    @Suppress("DEPRECATION")
    resolve()(color)
  }
}

/**
 * Maps the color provided by the color provider
 */
fun ThemeKey<ColorMapperNullable>.forColor(colorProvider: ColorProvider): ColorProviderNullable {
  return {
    @Suppress("DEPRECATION")
    resolve()(colorProvider())
  }
}

/**
 * Maps a multi provider to a single value
 */
fun <IndexContext, T> ThemeKey<MultiProvider<IndexContext, T>>.mapped(mapping: MultiProvider<IndexContext, T>.() -> T): () -> T {
  return {
    val multiProvider: MultiProvider<IndexContext, T> = this.resolve()

    mapping(multiProvider)
  }
}

fun <IndexContext, T> ThemeKey<MultiProvider<IndexContext, T>>.valueAt(index: Int): () -> T {
  return mapped { valueAt(index) }
}
