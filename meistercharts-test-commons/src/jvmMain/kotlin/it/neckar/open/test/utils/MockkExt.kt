package it.neckar.open.test.utils

import assertk.*
import io.mockk.Called
import io.mockk.MockKVerificationScope
import io.mockk.Ordering
import io.mockk.clearMocks
import io.mockk.isMockKMock
import io.mockk.verify

/**
 * Clears this mock.
 * Must only be called if this is a mockk mock.
 */
fun Any.clearMock() {
  requireIsMock()
  clearMocks(this)
}

fun Any.requireIsMock() {
  require(isMockKMock(this)) {
    "This [$this] is not a mock"
  }
}

/**
 * Ensures that the mock was not called
 */
fun Assert<Any>.mockWasNotCalled(): Unit = given {
  it.requireIsMock()
  verify { it wasNot Called }
}

/**
 * Bridge to mockk's verify function
 */
fun Assert<Any>.mockWasCalled(
  ordering: Ordering = Ordering.UNORDERED,
  inverse: Boolean = false,
  atLeast: Int = 1,
  atMost: Int = Int.MAX_VALUE,
  exactly: Int = -1,
  timeout: Long = 0,
  verifyBlock: MockKVerificationScope.() -> Unit,
): Unit = given {
  it.requireIsMock()
  verify(ordering, inverse, atLeast, atMost, exactly, timeout, verifyBlock)
}
