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
package com.meistercharts.canvas.layout.cache

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
class LayoutVariableObjectCacheTest {
  @Test
  fun testIncreaseIfNecessary() {
    val layoutObject: LayoutVariable = mockk() {
      every { reset() }.returns(Unit)
    }

    var factoryCalled = false

    val cache = LayoutVariablesObjectCache {
      factoryCalled = true
      layoutObject
    }


    assertThat(factoryCalled).isFalse()
    assertThat(cache.size).isEqualTo(0)

    cache.addNewElement()
    assertThat(cache.size).isEqualTo(1)
    assertThat(factoryCalled).isTrue()
    factoryCalled = false

    cache.clear()
    assertThat(cache.size).isEqualTo(0)
    assertThat(factoryCalled).isFalse()

    cache.addNewElement()
    assertThat(cache.size).isEqualTo(1)
    assertThat(factoryCalled).isFalse()

    cache.addNewElement()
    assertThat(cache.size).isEqualTo(2)
    assertThat(factoryCalled).isTrue()
  }

  @Test
  fun testClearRecycle() {
    val layoutObject: LayoutVariable = mockk() {
      every { reset() }.returns(Unit)
    }

    var factoryCalled = false

    val cache = LayoutVariablesObjectCache {
      factoryCalled = true
      layoutObject
    }

    assertThat(factoryCalled).isFalse()
    cache.clear()
    assertThat(factoryCalled).isFalse()
    cache.prepare(5)
    assertThat(factoryCalled).isTrue()

    factoryCalled = false
    cache.clear()
    assertThat(factoryCalled).isFalse()

    cache.prepare(1) //does *not* create a new object
    assertThat(factoryCalled).isFalse()

    cache.prepare(5) //does *not* create a new object
    assertThat(factoryCalled).isFalse()

    cache.prepare(6) //creates a *single* new object
    assertThat(factoryCalled).isTrue()
  }

  @Test
  fun testClear() {
    val layoutObject: LayoutVariable = mockk()
    every { layoutObject.reset() }.returns(Unit)

    val cache = LayoutVariablesObjectCache { layoutObject }

    //Initial
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)
    assertThat(cache.size).isEqualTo(0)

    //Prepare 10
    cache.prepare(10)

    assertThat(cache.size).isEqualTo(10)
    assertThat(cache[0]).isSameAs(layoutObject)
    verify(exactly = 10) {
      layoutObject.reset()
    }
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)

    //Clear
    cache.clear() //resets the size to 0

    assertThat(cache.size).isEqualTo(0)
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)

    //Prepare 8
    cache.prepare(8)
    assertThat(cache.size).isEqualTo(8)
    assertThat(cache[0]).isSameAs(layoutObject)
    verify(exactly = 8) {
      layoutObject.reset()
    }
    confirmVerified(layoutObject)
    clearMocks(layoutObject, answers = false)
  }

  @Test
  fun testSortResize() {
    val cache = LayoutVariablesObjectCache {
      MyLayoutObject()
    }

    cache.ensureSize(10)
    cache.verifyInstancesMatch()
    cache.values.reverse()


    cache.ensureSize(11)
    cache.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added

    cache.ensureSize(8)
    cache.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added

    cache.ensureSize(13)
    cache.verifyInstancesMatch()
    //Ensure that no duplicate objects have been added
  }

  @Test
  fun testResize() {
    var creationCounter = 0

    val cache = LayoutVariablesObjectCache {
      creationCounter++
      MyLayoutObject()
    }


    assertThat(cache.size).isEqualTo(0)
    assertThat(cache.values).hasSize(0)
    assertThat(creationCounter).isEqualTo(0)

    //2 new objects are created
    cache.ensureSize(2)
    cache.verifyInstancesMatch()

    assertThat(cache.size).isEqualTo(2)
    assertThat(cache.values).hasSize(2)
    assertThat(creationCounter).isEqualTo(2)

    //Same size, nothing changes
    cache.ensureSize(2)
    cache.verifyInstancesMatch()

    assertThat(cache.size).isEqualTo(2)
    assertThat(cache.values).hasSize(2)
    assertThat(creationCounter).isEqualTo(2)

    //1 additional object is created
    cache.ensureSize(3)
    cache.verifyInstancesMatch()

    assertThat(cache.size).isEqualTo(3)
    assertThat(cache.values).hasSize(3)
    assertThat(creationCounter).isEqualTo(3)

    //Shrinking, no object is created
    cache.ensureSize(1)
    cache.verifyInstancesMatch()

    assertThat(cache.size).isEqualTo(1)
    assertThat(cache.values).hasSize(1) //list is not changed
    assertThat(creationCounter).isEqualTo(3)
  }

  @Test
  fun testSortForward() {
    val cache = LayoutVariablesObjectCache {
      MyLayoutObject()
    }

    cache.ensureSize(10)
    cache.verifyInstancesMatch()

    cache.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    cache.sortWith(Comparator { o1, o2 ->
      o1.y.compareTo(o2.y)
    })


    var counter = 0

    cache.fastForEachWithIndex { index, value ->
      assertThat(counter).isEqualTo(index)
      counter++
      assertThat(value.y).isEqualTo(index * 10.0)
    }

    assertThat(counter).isEqualTo(10)
  }

  @Test
  fun testSortBackwards() {
    val cache = LayoutVariablesObjectCache {
      MyLayoutObject()
    }

    cache.ensureSize(10)
    cache.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    cache.sortWith(Comparator { o1, o2 ->
      o2.y.compareTo(o1.y)
    })


    var counter = 0

    cache.fastForEachWithIndex { index, value ->
      assertThat(counter).isEqualTo(index)
      counter++
      assertThat(value.y).isEqualTo((9 - index) * 10.0)
    }

    assertThat(counter).isEqualTo(10)
  }

  @Test
  fun testSortBackwardReducedSizes() {
    val cache = LayoutVariablesObjectCache {
      MyLayoutObject()
    }

    cache.ensureSize(10)
    cache.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    //Now shrink

    cache.prepare(5)
    cache.fastForEachWithIndex { index, value ->
      value.y = index * 10.0
    }

    cache.sortWith(Comparator { o1, o2 ->
      o2.y.compareTo(o1.y)
    })


    var counter = 0

    cache.fastForEachWithIndex { index, value ->
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
private fun <T : LayoutVariable> LayoutVariablesObjectCache<T>.verifyInstancesMatch() {
  assertThat(values.size).isLessThanOrEqualTo(objectsStock.size)

  values.fastForEachIndexed { index, value ->
    assertThat(objectsStock[index]).isSameAs(value)
  }

  objectsStock.fastForEachIndexed { index, value ->
    if (index > 0) {
      val previous = objectsStock[index - 1]
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
