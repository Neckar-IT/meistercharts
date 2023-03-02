package com.meistercharts.algorithms.layout

import it.neckar.open.kotlin.lang.fastFor
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.data.Offset
import org.junit.jupiter.api.Test

/**
 */
class BoxLayoutCalculatorTest {
  @Test
  fun testFindIndex() {
    val layout = BoxLayoutCalculator.layout(
      availableSpace = 100.0,
      numberOfBoxes = 4,
      layoutDirection = LayoutDirection.LeftToRight,
      minBoxSize = 0.5,
      maxBoxSize = null,
      layoutMode = Exact
    )

    assertThat(layout.boxSize).isEqualTo(25.0)
    assertThat(layout.gap).isEqualTo(0.0)

    assertThat(layout.boxIndexFor(0.0)).isEqualTo(BoxIndex(0))
    assertThat(layout.boxIndexFor(-0.1)).isNull()

    assertThat(layout.boxIndexFor(24.999)).isEqualTo(BoxIndex(0))
    assertThat(layout.boxIndexFor(25.0)).isEqualTo(BoxIndex(1))
    assertThat(layout.boxIndexFor(25.1)).isEqualTo(BoxIndex(1))

    assertThat(layout.boxIndexFor(49.99)).isEqualTo(BoxIndex(1))
    assertThat(layout.boxIndexFor(50.0)).isEqualTo(BoxIndex(2))
    assertThat(layout.boxIndexFor(50.1)).isEqualTo(BoxIndex(2))

    assertThat(layout.boxIndexFor(74.999999)).isEqualTo(BoxIndex(2))
    assertThat(layout.boxIndexFor(75.0)).isEqualTo(BoxIndex(3))
    assertThat(layout.boxIndexFor(75.1)).isEqualTo(BoxIndex(3))

    assertThat(layout.boxIndexFor(99.999)).isEqualTo(BoxIndex(3))
    assertThat(layout.boxIndexFor(100.0)).isNull()
    assertThat(layout.boxIndexFor(100.1)).isNull()


    //Verify all
    verifyLayoutIndexFor(layout)
  }

  @Test
  fun testFindIndexWithGaps() {
    val layout = BoxLayoutCalculator.layout(
      availableSpace = 100.0,
      numberOfBoxes = 4,
      layoutDirection = LayoutDirection.LeftToRight,
      minBoxSize = 0.5,
      maxBoxSize = 20.0,
      gapSize = 5.0,
      layoutMode = Exact
    )

    assertThat(layout.boxSize).isEqualTo(20.0)
    assertThat(layout.gap).isEqualTo(5.0)

    verifyLayoutIndexFor(layout)
  }

  private fun verifyLayoutIndexFor(layout: EquisizedBoxLayout) {
    val gapTestDelta = 0.1

    layout.numberOfBoxes.fastFor { i ->
      val boxIndex = BoxIndex(i)

      val start = layout.calculateStart(boxIndex)
      val end = layout.calculateEnd(boxIndex)
      val center = layout.calculateCenter(boxIndex)

      //Exact hit
      assertThat(layout.boxIndexFor(start)).describedAs("boxIndex $boxIndex - start: $start").isEqualTo(boxIndex)
      assertThat(layout.boxIndexFor(center)).describedAs("boxIndex $boxIndex - center: $center").isEqualTo(boxIndex)

      //when gap is 0.0 - the end is part of the next box
      assertThat(layout.boxIndexFor(end - 0.1)).describedAs("boxIndex $boxIndex - end: $end").isEqualTo(boxIndex)

      if (layout.gap > gapTestDelta) {
        //We have gaps!
        assertThat(layout.boxIndexFor(end)).describedAs("boxIndex $boxIndex - end: $end").isEqualTo(boxIndex)

        assertThat(layout.boxIndexFor(start - gapTestDelta)).isNull()
        assertThat(layout.boxIndexFor(end + 0.1)).isNull()
      }
    }
  }

  @Test
  fun testFindIndexWithGapsRL() {
    val layout = BoxLayoutCalculator.layout(
      availableSpace = 100.0,
      numberOfBoxes = 4,
      layoutDirection = LayoutDirection.RightToLeft,
      minBoxSize = 0.5,
      maxBoxSize = 20.0,
      gapSize = 5.0,
      layoutMode = Exact
    )

    assertThat(layout.boxSize).isEqualTo(20.0)
    assertThat(layout.gap).isEqualTo(5.0)

    verifyLayoutIndexFor(layout)
  }

  @Test
  fun testFindIndexWithGaps2() {
    val layout = BoxLayoutCalculator.layout(
      availableSpace = 100.0,
      numberOfBoxes = 4,
      layoutDirection = LayoutDirection.CenterVertical,
      minBoxSize = 0.5,
      maxBoxSize = 20.0,
      gapSize = 5.0,
      layoutMode = Exact
    )

    assertThat(layout.boxSize).isEqualTo(20.0)
    assertThat(layout.gap).isEqualTo(5.0)

    verifyLayoutIndexFor(layout)
  }

  @Test
  fun testPixelMode() {
    BoxLayoutCalculator.layout(101.7, 4, LayoutDirection.LeftToRight, 0.43, null, layoutMode = Exact).let { layout ->
      assertThat(layout.boxSize).isEqualTo(25.425)
      assertThat(layout.gap).isEqualTo(0.0)
      assertThat(layout.availableSpace).isEqualTo(101.7)
      assertThat(layout.remainingSpace).isEqualTo(0.0)
      assertThat(layout.usedSpace).isEqualTo(101.7)
      assertThat(layout.layoutDirection).isEqualTo(LayoutDirection.LeftToRight)
      assertThat(layout.totalGaps).isEqualTo(0.0)
    }

    BoxLayoutCalculator.layout(101.7, 4, LayoutDirection.LeftToRight, 0.43, null, layoutMode = Rounded).let { layout ->
      assertThat(layout.availableSpace).isEqualTo(101.7)
      assertThat(layout.boxSize).isEqualTo(25.0)
      assertThat(layout.gap).isEqualTo(0.0)
      assertThat(layout.remainingSpace).isCloseTo(1.7, Offset.offset(0.000001))
      assertThat(layout.usedSpace).isEqualTo(100.0)
      assertThat(layout.layoutDirection).isEqualTo(LayoutDirection.LeftToRight)
      assertThat(layout.totalGaps).isEqualTo(0.0)
    }
  }

  @Test
  internal fun testSimple() {
    assertThat(calculateBoxSize(100.0, 1, 0.0, null, 0.0, Exact)).isEqualTo(100.0)
    assertThat(calculateBoxSize(100.0, 2, 0.0, null, 0.0, Exact)).isEqualTo(50.0)
    assertThat(calculateBoxSize(100.0, 4, 0.0, null, 0.0, Exact)).isEqualTo(25.0)
  }

  @Test
  internal fun testLocation() {
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).boxSize).isEqualTo(25.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateCenter(BoxIndex(0))).isEqualTo(12.5)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateCenter(BoxIndex(1))).isEqualTo(37.5)
  }

  @Test
  internal fun testStartEnd() {
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).boxSize).isEqualTo(25.0)

    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateStart(BoxIndex(0))).isEqualTo(0.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateStart(BoxIndex(1))).isEqualTo(25.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateStart(BoxIndex(2))).isEqualTo(50.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateStart(BoxIndex(3))).isEqualTo(75.0)

    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateEnd(BoxIndex(0))).isEqualTo(25.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateEnd(BoxIndex(1))).isEqualTo(50.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateEnd(BoxIndex(2))).isEqualTo(75.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).calculateEnd(BoxIndex(3))).isEqualTo(100.0)
  }

  @Test
  fun testStartEndCenterWithoutGap() {
    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.CenterHorizontal, 0.0, null).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(10.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(5.0)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(25.0)
    }

    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.LeftToRight, 0.0, null).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(10.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(5.0)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(25.0)
    }

    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.RightToLeft, 0.0, null).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(10.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(5.0)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(25.0)
    }
  }

  @Test
  fun testStartEndCenterWithMaxSize() {
    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.LeftToRight, 0.0, 8.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(8.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(4.0)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(4.0 + 8)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(4.0 + 8 + 8)
    }

    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.RightToLeft, 0.0, 8.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(8.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(30.0 - 4 - 16)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(30.0 - 4 - 8)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(30.0 - 4)
    }


    //Odd box count
    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.CenterHorizontal, 0.0, 8.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(8.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(15.0 - 8)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(15.0 + 8)
    }

    //Even box count
    BoxLayoutCalculator.layout(30.0, 4, LayoutDirection.CenterHorizontal, 0.0, 5.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(4)
      assertThat(it.boxSize).isEqualTo(5.0)
      assertThat(it.availableSpace).isEqualTo(30.0)
      assertThat(it.remainingSpace).isEqualTo(10.0)
      assertThat(it.usedSpace).isEqualTo(5.0 * 4)
      assertThat(it.totalGaps).isEqualTo(0.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(15.0 - 5 - 2.5)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0 - 2.5)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(15.0 + 2.5)
      assertThat(it.calculateCenter(BoxIndex(3))).isEqualTo(15.0 + 5 + 2.5)
    }

    //With gaps!

    //Odd box count
    BoxLayoutCalculator.layout(30.0, 3, LayoutDirection.CenterHorizontal, 0.0, 5.0, 1.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.boxSize).isEqualTo(5.0)
      assertThat(it.gap).isEqualTo(1.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(15.0 - 5 - 1)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(15.0 + 5 + 1)
    }

    //Event box count
    BoxLayoutCalculator.layout(30.0, 4, LayoutDirection.CenterHorizontal, 0.0, 5.0, 1.0).let {
      assertThat(it.numberOfBoxes).isEqualTo(4)
      assertThat(it.boxSize).isEqualTo(5.0)
      assertThat(it.gap).isEqualTo(1.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(15.0 - 0.5 - 2.5 - 1 - 5)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(15.0 - 0.5 - 2.5)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(15.0 + 0.5 + 2.5)
      assertThat(it.calculateCenter(BoxIndex(3))).isEqualTo(15.0 + 0.5 + 2.5 + 1 + 5)
    }
  }

  @Test
  internal fun testRemainingWidth() {
    assertThat(BoxLayoutCalculator.layout(100.0, 1, LayoutDirection.LeftToRight, 0.0, null).usedSpace).isEqualTo(100.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 2, LayoutDirection.LeftToRight, 0.0, null).boxSize).isEqualTo(50.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 2, LayoutDirection.LeftToRight, 0.0, null).usedSpace).isEqualTo(100.0)
    assertThat(BoxLayoutCalculator.layout(100.0, 7, LayoutDirection.LeftToRight, 0.0, null).usedSpace).isEqualTo(100.0, Offset.offset(0.000000001))
    assertThat(BoxLayoutCalculator.layout(100.0, 1, LayoutDirection.LeftToRight, 0.0, null).remainingSpace).isEqualTo(0.0)

    BoxLayoutCalculator.layout(100.0, 1, LayoutDirection.LeftToRight, 0.0, 5.0).let {
      assertThat(it.remainingSpace).isEqualTo(95.0)
      assertThat(it.boxSize).isEqualTo(5.0)
    }
  }

  @Test
  fun testWithGap() {
    BoxLayoutCalculator.layout(100.0, 3, LayoutDirection.LeftToRight, 0.0, null, 5.0).let {
      assertThat(it.availableSpace).isEqualTo(100.0)
      assertThat(it.usedSpace).isEqualTo(100.0)
      assertThat(it.usedSpaceWithoutGaps).isEqualTo(90.0)
      assertThat(it.totalGaps).isEqualTo(10.0)
      assertThat(it.remainingSpace).isEqualTo(0.0)

      assertThat(it.numberOfBoxes).isEqualTo(3)
      assertThat(it.gap).isEqualTo(5.0)
      assertThat(it.boxSize).isEqualTo(30.0)

      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(15.0)
      assertThat(it.calculateStart(BoxIndex(0))).isEqualTo(00.0)
      assertThat(it.calculateEnd(BoxIndex(0))).isEqualTo(30.0)

      assertThat(it.calculateStart(BoxIndex(1))).isEqualTo(35.0)
      assertThat(it.calculateCenter(BoxIndex(1))).isEqualTo(50.0)
      assertThat(it.calculateEnd(BoxIndex(1))).isEqualTo(65.0)

      assertThat(it.calculateStart(BoxIndex(2))).isEqualTo(70.0)
      assertThat(it.calculateCenter(BoxIndex(2))).isEqualTo(85.0)
      assertThat(it.calculateEnd(BoxIndex(2))).isEqualTo(100.0)
    }
  }

  @Test
  fun testMaxSize() {
    BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 0.0, null).let {
      assertThat(it.availableSpace).isEqualTo(100.0)
      assertThat(it.numberOfBoxes).isEqualTo(4)
      assertThat(it.boxSize).isEqualTo(25.0)

      assertThat(it.calculateStart(BoxIndex(0))).isEqualTo(0.0)
      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(12.5)
      assertThat(it.calculateEnd(BoxIndex(0))).isEqualTo(25.0)
    }

    BoxLayoutCalculator.layout(100.0, 4, LayoutDirection.LeftToRight, 20.0, null).let {
      assertThat(it.availableSpace).isEqualTo(100.0)
      assertThat(it.numberOfBoxes).isEqualTo(4)
      assertThat(it.boxSize).isEqualTo(25.0)

      assertThat(it.calculateStart(BoxIndex(0))).isEqualTo(0.0)
      assertThat(it.calculateCenter(BoxIndex(0))).isEqualTo(12.5)
      assertThat(it.calculateEnd(BoxIndex(0))).isEqualTo(25.0)
    }
  }
}
