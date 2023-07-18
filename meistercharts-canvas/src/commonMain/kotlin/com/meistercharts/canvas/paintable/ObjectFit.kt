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
package com.meistercharts.canvas.paintable

/**
 * Specifies how the content should be resized to fit its container.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/CSS/object-fit">object-fit</a>
 */
enum class ObjectFit(
  /**
   * True, if the aspect ratio is maintained
   */
  val aspectRatioMaintained: Boolean,
  /**
   * True, if the object can be scaled down
   */
  val mayBeScaledDown: Boolean,
  /**
   * True, if the object can be scaled up
   */
  val mayBeScaledUp: Boolean,
) {
  /**
   * The content is not resized.
   * The original size is retained.
   */
  None(true, false, false),

  /**
   * The content is sized to fill the container's box.
   * The content will completely fill the box.
   * If the content's aspect ratio does not match the aspect ratio of its box, then the content will be stretched to fit.
   */
  Fill(false, true, true),

  /**
   * The content is scaled (up *or* down) to maintain its aspect ratio while fitting within the container’s box.
   * The content is made to fill the box, while preserving its aspect ratio, so the content will be "letterboxed" if its aspect ratio does not match the aspect ratio of the box.
   */
  Contain(true, true, true),

  /**
   * The content is scaled (down) to maintain its aspect ratio while fitting within the container’s box.
   * The content is made to fill the box, while preserving its aspect ratio, so the content will be "letterboxed" if its aspect ratio does not match the aspect ratio of the box.
   *
   * If the content is smaller than the bounding box, the content will be centered.
   */
  ContainNoGrow(true, true, false),
}
