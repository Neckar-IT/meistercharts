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
package com.meistercharts.canvas.interaction

import com.meistercharts.annotations.Window
import com.meistercharts.annotations.Zoomed
import com.meistercharts.canvas.layout.buffer.BoundsMultiBuffer
import it.neckar.geometry.Coordinates
import it.neckar.geometry.Rectangle
import it.neckar.geometry.RightTriangleType
import it.neckar.open.annotations.Allocates
import it.neckar.open.annotations.AllocationCost
import it.neckar.open.collections.fastForEach
import it.neckar.open.unit.number.MayBeNegative

/**
 * Collects the interactive regions of a planning chart for one layout pass and answers hit tests against them.
 *
 * There is exactly one hit order, so hover and press always resolve to the same region.
 * The order is: higher [HandleRole.hitPriority] first, then registration order - the region registered last wins.
 *
 * Registration order therefore has to mirror the paint order. The layer that paints first calls [beginLayout] at the
 * start of its layout method and registers its regions; layers that paint later register afterwards without calling
 * [beginLayout] again.
 *
 * ```
 * // PvRoofPlanningLayer.layout - paints first, owns the layout pass
 * interactionTargets.beginLayout()
 * moduleAreas.fastForEach { interactionTargets.register(it, HandleRole.Body, x, y, width, height) }
 *
 * // PvModulePlanningLayer.layout - paints on top, adds the buttons
 * moduleAreas.fastForEach { interactionTargets.register(it, HandleRole.DeleteButton, x, y, width, height) }
 * ```
 */
class InteractionTargets<E : Any> {
  /**
   * The regions, index-aligned with [elements] and [roles].
   */
  private val bounds: @Window BoundsMultiBuffer = BoundsMultiBuffer()

  /**
   * The element each region belongs to.
   */
  private val elements: MutableList<E> = mutableListOf()

  /**
   * What each region does for its element.
   */
  private val roles: MutableList<HandleRole> = mutableListOf()

  /**
   * Which triangle of [bounds] a region covers - null for a region that covers the whole rectangle.
   */
  private val rightTriangleTypes: MutableList<RightTriangleType?> = mutableListOf()

  /**
   * How many regions are registered for the current layout pass.
   */
  val size: Int
    get() = elements.size

  /**
   * Drops the regions of the previous layout pass. Called by the layer that paints first.
   */
  fun beginLayout() {
    elements.clear()
    roles.clear()
    rightTriangleTypes.clear()
    bounds.resize(0)
  }

  /**
   * Registers one interactive region.
   */
  fun register(
    element: E,
    role: HandleRole,
    x: @Window Double,
    y: @Window Double,
    width: @Zoomed @MayBeNegative Double,
    height: @Zoomed @MayBeNegative Double,
    /**
     * Set to cover only one triangle of the given rectangle instead of the whole rectangle.
     */
    rightTriangleType: RightTriangleType? = null,
  ) {
    val index = elements.size
    elements.add(element)
    roles.add(role)
    rightTriangleTypes.add(rightTriangleType)

    bounds.resize(index + 1)
    bounds.x(index, x)
    bounds.y(index, y)
    bounds.width(index, width)
    bounds.height(index, height)
  }

  /**
   * Registers one interactive region from an already calculated rectangle.
   */
  fun register(
    element: E,
    role: HandleRole,
    regionBounds: @Window Rectangle,
    /**
     * Set to cover only one triangle of the given rectangle instead of the whole rectangle.
     */
    rightTriangleType: RightTriangleType? = null,
  ) {
    register(element, role, regionBounds.getX(), regionBounds.getY(), regionBounds.getWidth(), regionBounds.getHeight(), rightTriangleType)
  }

  /**
   * Returns true if the region at the given index contains the location.
   */
  private fun contains(index: Int, locationX: @Window Double, locationY: @Window Double): Boolean {
    val rightTriangleType = rightTriangleTypes[index] ?: return true

    return rightTriangleType.isPointOnFilledSide(
      pointX = locationX,
      pointY = locationY,
      x = bounds.x(index),
      y = bounds.y(index),
      width = bounds.width(index),
      height = bounds.height(index),
    )
  }

  /**
   * Returns the region that wins at the given location, or null if no region contains it.
   */
  @Allocates(AllocationCost.Constant)
  fun findTargetAt(location: @Window Coordinates): InteractionTarget<E>? {
    return findTargetAt(location.x, location.y)
  }

  /**
   * Returns the region that wins at the given location, or null if no region contains it.
   */
  @Allocates(AllocationCost.Constant)
  fun findTargetAt(locationX: @Window Double, locationY: @Window Double): InteractionTarget<E>? {
    HandleRole.descendingHitPriorities.fastForEach { hitPriority ->
      bounds.findLastIndex(locationX, locationY) { index ->
        roles[index].hitPriority == hitPriority && contains(index, locationX, locationY)
      }?.let { index ->
        return InteractionTarget(elements[index], roles[index], bounds.asRect(index))
      }
    }

    return null
  }

  /**
   * Returns the element that wins at the given location for the given role, or null if no such region contains it.
   */
  fun findElementAt(location: @Window Coordinates, role: HandleRole): E? {
    return bounds.findLastIndex(location.x, location.y) { index ->
      roles[index] == role && contains(index, location.x, location.y)
    }?.let { index ->
      elements[index]
    }
  }
}
