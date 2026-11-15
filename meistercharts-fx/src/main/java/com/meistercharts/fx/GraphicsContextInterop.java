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
package com.meistercharts.fx;

import it.neckar.open.annotations.Hot;
import javafx.scene.canvas.GraphicsContext;

/**
 * Java interop for {@link GraphicsContext} vararg methods.
 * <p>
 * Kotlin can only call a Java vararg method with the spread operator ({@code *array}), which copies
 * the array on <em>every</em> call. Java passes the array as-is - this helper avoids the per-call copy
 * on the render hot path. {@link GraphicsContext#setLineDashes(double...)} copies the values into its
 * own state internally, so passing a shared array is safe.
 */
final class GraphicsContextInterop {
  private GraphicsContextInterop() {
  }

  /**
   * Calls {@link GraphicsContext#setLineDashes(double...)} without copying the array.
   */
  @Hot
  static void setLineDashes(GraphicsContext context, double[] dashes) {
    context.setLineDashes(dashes);
  }
}
