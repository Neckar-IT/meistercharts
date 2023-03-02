package com.meistercharts.canvas.layout.cache

import assertk.*
import assertk.assertions.*
import it.neckar.open.collections.fastForEachIndexed
import org.junit.jupiter.api.Test

/**
 *
 */
class LayoutVariableObjectCacheTest {
  @Test
  fun testSortResize() {
    val cache = LayoutVariableObjectCache {
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

    val cache = LayoutVariableObjectCache {
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
    val cache = LayoutVariableObjectCache {
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
    val cache = LayoutVariableObjectCache {
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
    val cache = LayoutVariableObjectCache {
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
private fun <T : LayoutVariable> LayoutVariableObjectCache<T>.verifyInstancesMatch() {
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
