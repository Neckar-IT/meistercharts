package com.meistercharts.algorithms

import it.neckar.open.annotations.Boxed
import kotlin.jvm.JvmInline

/**
 * A reason for an update to the zoom or pan.
 */
@JvmInline
value class UpdateReason(val value: Int) {
  override fun toString(): String {
    return "UpdateReason(value=0b_${value.toString(2)})"
  }

  /**
   * Returns the label for the update reason.
   */
  fun label(): String {
    return entryLabels[this] ?: "Nothing found! ${value.toString(2)}"
  }

  companion object {
    /**
     * Only for the initialization
     */
    val Initial: UpdateReason = UpdateReason(0b0000000_00000000_00000000_00000001)

    /**
     * Is used for updates due to a user interaction (e.g. by mouse or touch or using the toolbar)
     */
    val UserInteraction: UpdateReason = UpdateReason(0b0000000_01000000_00000000_10100000)

    /**
     * Is used for updates due to an animation
     */
    val Animation: UpdateReason = UpdateReason(0b0000000_00000000_00010000_00100000)

    /**
     * Changes due to updated data (e.g. new data points) - related to [AutoScale]
     */
    val DataChanged: UpdateReason = UpdateReason(0b0000000_00000010_00000000_00100000)

    /**
     * Call by the API
     */
    val ApiCall: UpdateReason = UpdateReason(0b0000000_00100010_00000000_00100000)

    /**
     * Changes due to auto-scale
     */
    val AutoScale: UpdateReason = UpdateReason(0b0000000_00000000_00000000_00100000)

    /**
     * Changes due to a configuration update (e.g. content viewport margin)
     */
    val ConfigurationUpdate: UpdateReason = UpdateReason(0b0000000_00000000_00000100_00100000)

    /**
     * Changes to to an environment updated (e.g. device pixel ratio)
     */
    val EnvironmentUpdate: UpdateReason = UpdateReason(0b0000100_00000000_00000100_00100000)

    /**
     * Is used for updates due to a (browser) window resize
     */
    val WindowResize: UpdateReason = UpdateReason(0b0000000_00000000_00000000_10100000)

    /**
     * Is used when the chart state is updated from another chart (bound charts)
     */
    val BoundToOtherChart: UpdateReason = UpdateReason(0b0000000_01000000_01000000_10100000)

    /**
     * Represents an unknown reason. Should only be used for backwards compatibility.
     */
    val Unknown: UpdateReason = UpdateReason(0b1000000_00000000_00000000_00000000)


    /**
     * Contains all entries.
     *
     * ATTENTION: Do *not* use in production code, since the elements are boxed!
     *
     * These values are verified by a unit test.
     * If the test fails the updated code will be generated on the console.
     */
    @Boxed
    val entries: List<UpdateReason> = listOf(
      Animation, ApiCall, AutoScale, BoundToOtherChart, ConfigurationUpdate, DataChanged, EnvironmentUpdate, Initial, Unknown, UserInteraction, WindowResize
    )

    /**
     * Contains the labels
     *
     *
     * These values are verified by a unit test.
     * If the test fails the updated code will be generated on the console.
     */
    @Boxed
    val entryLabels: Map<UpdateReason, String> = mapOf(
      Animation to "Animation",
      ApiCall to "ApiCall",
      AutoScale to "AutoScale",
      BoundToOtherChart to "BoundToOtherChart",
      ConfigurationUpdate to "ConfigurationUpdate",
      DataChanged to "DataChanged",
      EnvironmentUpdate to "EnvironmentUpdate",
      Initial to "Initial",
      Unknown to "Unknown",
      UserInteraction to "UserInteraction",
      WindowResize to "WindowResize"
    )
  }
}
