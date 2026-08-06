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
@file:Suppress("RETURN_VALUE_NOT_USED") // Test/demo-scope discards: builder-style mutator returns, event-notifier returns, subscription discards

package com.meistercharts.canvas.layout.buffer

import assertk.*
import assertk.assertions.*
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import it.neckar.open.collections.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class LayoutVariableObjectBufferTest {
  @Test
  fun testIncreaseIfNecessary() {
    val layoutObject: LayoutVariable = mockk {
      every { reset() }.returns(Unit)
    }

    var factoryCalled = false

    val buffer = LayoutVariablesObjectBuffer {
      factoryCalled = true
      layoutObject
    }


    assertThat(factoryCalled).isFalse()
    assertThat(buffer.size).isEqualTo(0)

    buffer.addNewElement()
    assertThat(buffer.size).isEqualTo(1)
    assertThat(factoryCalled).isTrue()
    factoryCalled = false

    buffer.clear()
    assertThat(buffer.size).isEqualTo(0)
    assertThat(factoryCalled).isFalse()

    buffer.addNewElement()
    assertThat(buffer.size).isEqualTo(1)
    assertThat(factoryCalled).isFalse()

    buffer.addNewElement()
    assertThat(buffer.size).isEqualTo(2)
    assertThat(factoryCalled).isTrue()
  }

  @Test
  fun testClearRecycle() {
    val layoutObject: LayoutVariable = mockk {
      every { reset() }.returns(Unit)
    }

    var factoryCalled = false

    val buffer = LayoutVariablesObjectBuffer {
      factoryCalled = true
      layoutObject
    }

    assertThat(factoryCalled).isFalse()
    buffer.clear()
    assertThat(factoryCalled).isFalse()
    buffer.prepare(5)
    assertThat(factoryCalled).isTrue()

    factoryCalled = false
    buffer.clear()
    assertThat(factoryCalled).isFalse()

    buffer.prepare(1) //does *not* create a new object
    assertThat(factoryCalled).isFalse()

    buffer.prepare(5) //does *not* create a new object
    assertThat(factoryCalled).isFalse()

    buffer.prepare(6) //creates a *single* new object
    assertThat(factoryCalled).isTrue()
  }

  @Test
  fun testClear() {
    val layoutObject: LayoutVariable = mockk()
    every { layoutObject.reset() }.returns(Unit)

    val buffer = LayoutVariablesObjectBuffer { layoutObject }

    //Initial
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)
    assertThat(buffer.size).isEqualTo(0)

    //Prepare 10
    buffer.prepare(10)

    assertThat(buffer.size).isEqualTo(10)
    assertThat(buffer[0]).isSameAs(layoutObject)
    verify(exactly = 10) {
      layoutObject.reset()
    }
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)

    buffer.clear() //resets the size to 0

    assertThat(buffer.size).isEqualTo(0)
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)

    //Prepare 8
    buffer.prepare(8)
    assertThat(buffer.size).isEqualTo(8)
    assertThat(buffer[0]).isSameAs(layoutObject)
    verify(exactly = 8) {
      layoutObject.reset()
    }
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)
  }

  @Test
  fun testSortResize() {
    val buffer = LayoutVariablesObjectBuffer {
      MyLayoutObject()
    }

    buffer.resize(10)
    buffer.verifyInstancesMatch()
    buffer.values.reverse()


    buffer.resize(11)
    buffer.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added

    buffer.resize(8)
    buffer.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added

    buffer.resize(13)
    buffer.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added
  }

  @Test
  fun testResize() {
    var creationCounter = 0

    val buffer = LayoutVariablesObjectBuffer {
      creationCounter++
      MyLayoutObject()
    }


    assertThat(buffer.size).isEqualTo(0)
    assertThat(buffer.values).hasSize(0)
    assertThat(creationCounter).isEqualTo(0)

    //2 new objects are created
    buffer.resize(2)
    buffer.verifyInstancesMatch()

    assertThat(buffer.size).isEqualTo(2)
    assertThat(buffer.values).hasSize(2)
    assertThat(creationCounter).isEqualTo(2)

    //Same size, nothing changes
    buffer.resize(2)
    buffer.verifyInstancesMatch()

    assertThat(buffer.size).isEqualTo(2)
    assertThat(buffer.values).hasSize(2)
    assertThat(creationCounter).isEqualTo(2)

    //1 additional object is created
    buffer.resize(3)
    buffer.verifyInstancesMatch()

    assertThat(buffer.size).isEqualTo(3)
    assertThat(buffer.values).hasSize(3)
    assertThat(creationCounter).isEqualTo(3)

    //Shrinking, no object is created
    buffer.resize(1)
    buffer.verifyInstancesMatch()

    assertThat(buffer.size).isEqualTo(1)
    assertThat(buffer.values).hasSize(1) //list is not changed
    assertThat(creationCounter).isEqualTo(3)
  }

  @Test
  fun testSortForward() {
    val buffer = LayoutVariablesObjectBuffer {
      MyLayoutObject()
    }

    buffer.resize(10)
    buffer.verifyInstancesMatch()

    buffer.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    buffer.sortWith(Comparator { o1, o2 ->
      o1.y.compareTo(o2.y)
    })


    var counter = 0

    buffer.fastForEachWithIndex { index, value ->
      assertThat(counter).isEqualTo(index)
      counter++
      assertThat(value.y).isEqualTo(index * 10.0)
    }

    assertThat(counter).isEqualTo(10)
  }

  @Test
  fun testSortBackwards() {
    val buffer = LayoutVariablesObjectBuffer {
      MyLayoutObject()
    }

    buffer.resize(10)
    buffer.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    buffer.sortWith(Comparator { o1, o2 ->
      o2.y.compareTo(o1.y)
    })


    var counter = 0

    buffer.fastForEachWithIndex { index, value ->
      assertThat(counter).isEqualTo(index)
      counter++
      assertThat(value.y).isEqualTo((9 - index) * 10.0)
    }

    assertThat(counter).isEqualTo(10)
  }

  @Test
  fun testSortBackwardReducedSizes() {
    val buffer = LayoutVariablesObjectBuffer {
      MyLayoutObject()
    }

    buffer.resize(10)
    buffer.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    //Now shrink

    buffer.prepare(5)
    buffer.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    buffer.sortWith(Comparator { o1, o2 ->
      o2.y.compareTo(o1.y)
    })


    var counter = 0

    buffer.fastForEachWithIndex { index, value ->
      assertThat(counter).isEqualTo(index)
      counter++
      assertThat(value.y).isEqualTo((4 - index) * 10.0)
    }

    assertThat(counter).isEqualTo(5)
  }
}

/**
 * Verifies the instances of the objects
 */
private fun <T : LayoutVariable> LayoutVariablesObjectBuffer<T>.verifyInstancesMatch() {
  assertThat(values.size).isLessThanOrEqualTo(objectPool.size)

  values.fastForEachIndexed { index, value ->
    assertThat(objectPool[index]).isSameAs(value)
  }

  objectPool.fastForEachIndexed { index, value ->
    if (index > 0) {
      val previous = objectPool[index - 1]
      assertThat(previous).isNotSameAs(value)
    }
  }
}

class MyLayoutObject : LayoutVariable {
  var y: Double = 0.0

  override fun reset() {
    y = 0.0
  }
}
