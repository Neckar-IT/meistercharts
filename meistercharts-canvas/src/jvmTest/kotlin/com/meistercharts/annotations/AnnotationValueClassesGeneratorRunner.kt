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
package com.meistercharts.annotations

/**
 * Helper class that creates code that has been copied to ChartCalculator.
 * This helper should be no longer necessary
 */
fun main() {
  Creator().also {
    it.createDeclarations()

    println("------- Chart Calculator ----------------------------------")

    it.createCalculatorMethods()
  }

}


class Creator {
  fun createDeclarations() {
    Application.values().forEach {
      createDeclarations(it)
    }
  }

  fun createCalculatorMethods() {
    //direct
    val count = Application.values().size

    for (i in 0..count - 2) {
      val source = Application.values()[i]
      val target = Application.values()[i + 1]

      if (source == Application.Domain || target == Application.Domain) {
        continue
      }

      Usage.values().forEach {
        //Forward
        println(createDirectCalculatorMethods(source, target, it))

        //Backward
        println(createDirectCalculatorMethods(target, source, it))
      }
    }

    //indirect - via 1 value

    for (i in 0..count - 3) {
      val source = Application.values()[i]
      val via = Application.values()[i + 1]
      val target = Application.values()[i + 2]

      if (source == Application.Domain || target == Application.Domain) {
        continue
      }

      Usage.values().forEach {
        //Forward
        println(createIndirectCalculatorMethods(source, via, target, it))

        //Backward
        println(createIndirectCalculatorMethods(target, via, source, it))
      }
    }


    //indirect - via 2 values
    for (i in 0..count - 4) {
      val source = Application.values()[i]
      val via1 = Application.values()[i + 1]
      val via2 = Application.values()[i + 2]
      val target = Application.values()[i + 3]

      if (source == Application.Domain || target == Application.Domain) {
        continue
      }

      Usage.values().forEach {
        //Forward
        println(createIndirectCalculatorMethods(source, via1, via2, target, it))

        //Backward
        println(createIndirectCalculatorMethods(target, via2, via1, source, it))
      }
    }

    //indirect - via 3 values
    for (i in 0..count - 5) {
      val source = Application.values()[i]
      val via1 = Application.values()[i + 1]
      val via2 = Application.values()[i + 2]
      val via3 = Application.values()[i + 3]
      val target = Application.values()[i + 4]

      if (source == Application.Domain || target == Application.Domain) {
        continue
      }

      Usage.values().forEach {
        //Forward
        println(createIndirectCalculatorMethods(source, via1, via2, via3, target, it))

        //Backward
        println(createIndirectCalculatorMethods(target, via3, via2, via1, source, it))
      }
    }
  }

  private fun createIndirectCalculatorMethods(source: Application, via: Application, target: Application, usage: Usage): String {
    return createIndirectCalculatorMethods(source, listOf(via), target, usage)
  }

  private fun createIndirectCalculatorMethods(source: Application, via: Application, via2: Application, target: Application, usage: Usage): String {
    return createIndirectCalculatorMethods(source, listOf(via, via2), target, usage)
  }

  private fun createIndirectCalculatorMethods(source: Application, via: Application, via2: Application, via3: Application, target: Application, usage: Usage): String {
    return createIndirectCalculatorMethods(source, listOf(via, via2, via3), target, usage)
  }

  private fun createIndirectCalculatorMethods(source: Application, via: List<Application>, target: Application, usage: Usage): String {
    val sourceName = source.name
    val targetName = target.name

    val stringBuilder = StringBuilder()
    stringBuilder.append("fun ${sourceName}${usage.name}.to$targetName(): ${targetName}${usage.name} {\n")
    stringBuilder.append("  return ")

    val conversions = via.map {
      "to${it.name}()"
    }.toMutableList()
    conversions.add("to${targetName}()")

    stringBuilder.append(conversions.joinToString("."))


    stringBuilder.append("}")

    return stringBuilder.toString()
  }

  private fun createDirectCalculatorMethods(source: Application, target: Application, usage: Usage): String {
    val sourceName = source.name
    val targetName = target.name

    return """fun ${sourceName}${usage.name}.to$targetName(): ${targetName}${usage.name} {
      return ${targetName}${usage.name}(${sourceName.decapitalize()}2${targetName.decapitalize()}${usage.conversionMethodPostFix.toUpperCase()}(value))
    }
    """
  }

  private fun createDeclarations(application: Application) {
    val typeName = application.name
    val decapitalizedName = typeName.decapitalize()

    println("------------------ ${typeName}Value ------------------")

    //Basic value types
    println(
      """
      @$typeName
      @JvmInline value class ${typeName}ValueX(val value: Double){
        ${additionalCode(application, Usage.ValueX)}

          fun coerceAtMost(maximumValue: Double): ${typeName}ValueX {
            return ${typeName}ValueX(value.coerceAtMost(maximumValue))
          }

          fun coerceAtLeast(minimumValue: Double): ${typeName}ValueX {
            return ${typeName}ValueX(value.coerceAtLeast(minimumValue))
          }

          operator fun minus(substrahend: ${typeName}ValueX): ${typeName}ValueX {
            return ${typeName}ValueX(value - substrahend.value)
          }
      }

      fun min(first: ${typeName}ValueX, second: ${typeName}ValueX): ${typeName}ValueX {
        return ${typeName}ValueX(kotlin.math.min(first.value, second.value))
      }
      fun max(first: ${typeName}ValueX, second: ${typeName}ValueX): ${typeName}ValueX {
        return ${typeName}ValueX(kotlin.math.max(first.value, second.value))
      }


      @$typeName
      @JvmInline value class ${typeName}ValueY(val value: Double){
        ${additionalCode(application, Usage.ValueY)}

        fun coerceAtMost(maximumValue: Double): ${typeName}ValueY {
            return ${typeName}ValueY(value.coerceAtMost(maximumValue))
        }

        fun coerceAtLeast(minimumValue: Double): ${typeName}ValueY {
          return ${typeName}ValueY(value.coerceAtLeast(minimumValue))
        }

        operator fun minus(substrahend: ${typeName}ValueY): ${typeName}ValueY {
          return ${typeName}ValueY(value - substrahend.value)
        }
      }

      fun min(first: ${typeName}ValueY, second: ${typeName}ValueY): ${typeName}ValueY {
        return ${typeName}ValueY(kotlin.math.min(first.value, second.value))
      }
      fun max(first: ${typeName}ValueY, second: ${typeName}ValueY): ${typeName}ValueY {
        return ${typeName}ValueY(kotlin.math.max(first.value, second.value))
      }

      """.trimIndent()
    )

    //if (application.forCoordinates) {
    //}
    println(
      """
          @$typeName
          @JvmInline value class ${typeName}Coordinates(val value: Coordinates){
            val x: Double
              get() = value.x
            val y: Double
              get() = value.y
              ${additionalCode(application, Usage.Coordinates)}
          }

          fun Coordinates.as${typeName}(): ${typeName}Coordinates {
            return ${typeName}Coordinates(this)
          }

          fun Coordinates.Companion.${decapitalizedName}(width: Double, height: Double): ${typeName}Coordinates {
            return Coordinates.of(width, height).as${typeName}()
          }
          """.trimIndent()
    )

    if (application.forSizes) {
      println(
        """
          @${typeName}
          @JvmInline value class ${typeName}Size(val value: Size) {
            val width: Double
              get() = value.width
            val height: Double
              get() = value.height
              ${additionalCode(application, Usage.Size)}
          }

          fun Size.as${typeName}(): ${typeName}Size {
            return ${typeName}Size(this)
          }
          fun Size.Companion.${decapitalizedName}(width: Double, height: Double): ${typeName}Size {
            return Size.of(width, height).as${typeName}()
          }


        """.trimIndent()
      )
    }
  }

  /**
   * Returns additional code for a usage (if there is one)
   */
  private fun additionalCode(application: Application, usage: Usage): String {
    return additionalCodeGenerators[Pair(application, usage)]?.let {
      it().trimIndent()
    }.orEmpty()
  }

  /**
   * Contains additional methods for a type
   */
  private val additionalCodeGenerators = mutableMapOf<Pair<Application, Usage>, () -> String>()
    .apply {
      put(Pair(Application.ContentArea, Usage.ValueX)) {
        """fun toDomainRelativeValue(axisOrientation: AxisInversionInformation): DomainRelativeValueX {
          if (axisOrientation.axisInverted) {
            return DomainRelativeValueX(1 - value)
          }

          return DomainRelativeValueX(value)
        }"""
      }
      put(Pair(Application.ContentArea, Usage.ValueY)) {
        """
            fun toDomainRelativeValue(axisOrientation: AxisInversionInformation): DomainRelativeValueY {
              if (axisOrientation.axisInverted) {
                return DomainRelativeValueY(1 - value)
              }

              return DomainRelativeValueY(value)
            }
        """
      }
      put(Pair(Application.Domain, Usage.ValueX)) {
        """  fun toDomainRelative(valueRange: ValueRange): DomainRelativeValueX {
              return valueRange.toDomainRelative(this)
            }
        """
      }
      put(Pair(Application.Domain, Usage.ValueY)) {
        """  fun toDomainRelative(valueRange: ValueRange): DomainRelativeValueY {
              return valueRange.toDomainRelative(this)
            }
        """
      }
      put(Pair(Application.DomainRelative, Usage.ValueX)) {
        """
          fun toContentAreaRelative(axisOrientation: AxisInversionInformation): ContentAreaRelativeValueX {
            if (axisOrientation.axisInverted) {
              return ContentAreaRelativeValueX(1 - value)
            }

            return ContentAreaRelativeValueX(value)
          }

          fun toDomain(valueRange: ValueRange): DomainValueX {
            return valueRange.toDomain(this)
          }
        """
      }
      put(Pair(Application.DomainRelative, Usage.ValueY)) {
        """
          fun toContentAreaRelative(axisOrientation: AxisInversionInformation): ContentAreaRelativeValueY {
            if (axisOrientation.axisInverted) {
              return ContentAreaRelativeValueY(1 - value)
            }

            return ContentAreaRelativeValueY(value)
          }

          fun toDomain(valueRange: ValueRange): DomainValueY {
            return valueRange.toDomain(this)
          }
        """
      }
    }


  /**
   * The annotation type / application for a value
   */
  enum class Application(
    val forCoordinates: Boolean,
    val forSizes: Boolean
  ) {
    Domain(true, true),
    DomainRelative(true, true),
    ContentAreaRelative(true, true),
    ContentArea(true, true),
    Zoomed(true, true),
    Window(true, false)
  }

  /**
   * The usage type
   */
  enum class Usage(val conversionMethodPostFix: String) {
    ValueX("x"),
    ValueY("y"),
    Coordinates(""),
    Size("")
  }
}

