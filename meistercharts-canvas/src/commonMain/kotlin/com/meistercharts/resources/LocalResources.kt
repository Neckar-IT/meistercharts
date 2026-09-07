/*
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
package com.meistercharts.resources

import it.neckar.open.http.Url

/**
 * Where the local resources of the charts ([LocalResourcePaintable]) are served from.
 */
object LocalResources {
  /**
   * Prepended to the relative path of every [LocalResourcePaintable] on the web targets, where the
   * path becomes the `src` of an image.
   *
   * `null` leaves the path relative to the document, so the browser resolves it against the current
   * address — right for an application served from one address, wrong for a single page application
   * whose routes have several path segments, because there every route resolves the same path
   * elsewhere. Such an application sets the directory its resources are served from, e.g.
   * `Url.rootRelative("/")`.
   *
   * Set it before the first chart is built; a paintable resolves its path once, at construction.
   */
  var basePath: Url.RootRelative? = null

  /**
   * Resolves [relativePath] against [basePath].
   */
  fun resolve(relativePath: Url): Url = basePath?.plus(relativePath.value) ?: relativePath
}
