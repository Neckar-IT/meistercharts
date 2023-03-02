package com.meistercharts.history

import com.meistercharts.history.impl.requireIsFinite
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 *
 */
class ValidationKtTest {
  @Test
  fun testValidateNumber() {
    17.0.requireIsFinite { "asdf" }
    assertThrows<IllegalArgumentException> { Double.NaN.requireIsFinite { "asdf" } }
    assertThrows<IllegalArgumentException> { Double.NEGATIVE_INFINITY.requireIsFinite { "asdf" } }
    assertThrows<IllegalArgumentException> { Double.POSITIVE_INFINITY.requireIsFinite { "asdf" } }
  }
}
