package com.meistercharts.service

import com.meistercharts.canvas.DevicePixelRatioSupport
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

/**
 */
class ServiceRegistryTest {
  @Test
  fun testIt() {
    val serviceRegistry = ServiceRegistry()
    Assertions.assertThat(serviceRegistry.find(DevicePixelRatioSupport::class)).isNull()

    val resolved = serviceRegistry.get(DevicePixelRatioSupport::class) {
      DevicePixelRatioSupport()
    }

    Assertions.assertThat(resolved).isNotNull()
    Assertions.assertThat(resolved).isSameAs(serviceRegistry.find(DevicePixelRatioSupport::class))
  }
}
