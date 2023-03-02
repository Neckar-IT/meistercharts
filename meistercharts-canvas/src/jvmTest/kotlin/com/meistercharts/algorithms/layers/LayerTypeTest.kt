package com.meistercharts.algorithms.layers

import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.junit.jupiter.api.Test

/**
 */
internal class LayerTypeTest {
  @Test
  internal fun testIt() {
    assertThat(LayerType.Content.below(LayerType.Content)).isFalse()
    assertThat(LayerType.Content.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Content.below(LayerType.Notification)).isTrue()

    assertThat(LayerType.Background.below(LayerType.Content)).isTrue()
    assertThat(LayerType.Background.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Background.below(LayerType.Notification)).isTrue()

    assertThat(LayerType.Notification.below(LayerType.Content)).isFalse()
    assertThat(LayerType.Notification.below(LayerType.Background)).isFalse()
    assertThat(LayerType.Notification.below(LayerType.Notification)).isFalse()
  }
}
