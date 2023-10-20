package it.neckar.ksp.ts

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSType
import it.neckar.ksp.format
import it.neckar.ksp.isPrimitiveArray

/**
 * Converts kotlin types to TypeScript types.
 */
object TypeScriptTypeSupport {
  /**
   * Returns the TypeScript type name for the given type.
   */
  fun toTypescriptType(type: KSType, logger: KSPLogger): String {
    val typeDeclaration = type.declaration
    val qualifiedName = typeDeclaration.qualifiedName?.asString() ?: throw IllegalArgumentException("No qualified name for ${typeDeclaration.simpleName.asString()} @ ${typeDeclaration.location.format()}")

    logger.info("getTypeName: $qualifiedName")

    if (qualifiedName == "kotlin.Unit") {
      return "void"
    }

    val nullableSuffix = when {
      type.isMarkedNullable -> " | null"
      else -> ""
    }

    return when {
      typeDeclaration.isPrimitiveArray() -> {
        typeDeclaration.simpleName.getShortName().toTypeScriptType() + "[]$nullableSuffix"
      }

      qualifiedName == "kotlin.Array" -> {
        val typeArguments = type.arguments

        require(typeArguments.size == 1) {
          "Expected exactly one type argument for Array, but got $typeArguments"
        }

        val firstTypeArgument = typeArguments.first()
        val firstTypeArgumentResolved = firstTypeArgument.type?.resolve()
        requireNotNull(firstTypeArgumentResolved) {
          "Could not resolve type argument $firstTypeArgument"
        }

        val typescriptType = toTypescriptType(firstTypeArgumentResolved, logger)
        if (typescriptType.contains(" ")) {
          "($typescriptType)[]$nullableSuffix"
        } else {
          "$typescriptType[]$nullableSuffix"
        }
      }

      else -> {
        typeDeclaration.simpleName.getShortName().toTypeScriptType() + nullableSuffix
      }
    }
  }


  private val kotlin2tsTypeMap = mapOf(
    "String" to "string",
    "Int" to "number",
    "Double" to "number",
    "Boolean" to "boolean",
    "IntArray" to "Int32Array",
    "DoubleArray" to "number[]",
  )

  /**
   * Maps the kotlin type to a typescript type
   */
  fun mapKotlinType2ts(kotlinType: String): String {
    return kotlin2tsTypeMap[kotlinType] ?: kotlinType
  }

  private fun String.toTypeScriptType(): String {
    return mapKotlinType2ts(this)
  }

  /**
   * Returns the optional marker for the provided type.
   * Returns a blank string if the type is not nullable.
   */
  fun getOptionalMarker(ksType: KSType): String {
    return if (ksType.isMarkedNullable) {
      "?"
    } else {
      ""
    }
  }
}
