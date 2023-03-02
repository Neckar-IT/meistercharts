package com.meistercharts.fx.debug

import assertk.*
import it.neckar.open.javafx.test.JavaFxTest
import org.junit.jupiter.api.Test
import java.lang.ref.WeakReference

@JavaFxTest
class MemoryLeakVerifierTest {
  @Test
  fun testCleanup() {
    MemoryLeakVerifier(getRef()).assertGarbageCollected()
  }

  @Test
  fun testNotKleanup() {
    val myDataClass = MyDataClass("asdf")
    try {
      MemoryLeakVerifier(WeakReference(myDataClass)).assertGarbageCollected()
      fail("Where is the exception?")
    } catch (e: Exception) {
    }
  }


  private fun getRef() = WeakReference(MyDataClass("asdf"))

  data class MyDataClass(val name: String)
}
