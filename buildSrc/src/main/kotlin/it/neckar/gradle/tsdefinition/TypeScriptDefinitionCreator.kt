package it.neckar.gradle.tsdefinition

import kastree.ast.Node


/**
 * Mapping from a Kotlin type to a typescript type
 */
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
fun mapKotlinType2ts(type: String): String {
  return kotlin2tsTypeMap[type] ?: type
}

/**
 * Creates the type script definition
 */
class TypeScriptDefinitionCreator(
  val namespace: String
) {

  /**
   * Creates the type script definition
   */
  fun create(kotlinFileContents: List<String>): String {
    val builder = StringBuilder()

    //Header
    builder.appendLine("export as namespace $namespace;\n")

    kotlinFileContents.map {
      SingleFileTypeScriptDefinitionCreator(it).create()
    }.forEach {
      builder.appendLine(it)
    }

    return builder.toString()
  }
}

/**
 * Returns true if the given param is nullable
 */
fun Node.Decl.Func.Param.isNullable(): Boolean {
  val ref = type?.ref
  return ref.isNullable()
}

/**
 * Returns true if the given param is nullable
 */
fun Node.Decl.Property.Var.isNullable(): Boolean {
  val ref = type?.ref
  return ref.isNullable()
}

fun Node.TypeRef?.isNullable() = this is Node.TypeRef.Nullable

/**
 * Returns true if this is a simple type that represents Unit
 */
fun Node.TypeRef.isUnit(): Boolean {
  if (this !is Node.TypeRef.Simple) return false

  val pieces = this.pieces

  if (pieces.size != 1) {
    return false
  }

  val piece = pieces.first()
  return piece.name == "Unit"
}

/**
 * Returns the name of a reference (of a method argument)
 */
fun Node.TypeRef?.name(): String {
  return when (this) {
    is Node.TypeRef.Simple -> {
      val piece = pieces[0]
      val pieceName = piece.name

      val typeParameter = piece.typeParams
        .filterNotNull()
        .map() {
          it.ref.name()
        }
        .firstOrNull()

      if (pieceName == "Array") {
        "$typeParameter[]"
      } else {
        mapKotlinType2ts(pieceName)
      }
    }

    is Node.TypeRef.Nullable -> type.name()
    is Node.TypeRef.Dynamic -> "object"
    is Node.TypeRef.Func -> {

      val paramNameWithType = this.params.mapIndexed { index, param ->
        val paramName = param.name ?: "value$index"
        val paramTypeName = param.type.ref.name()
        "$paramName: $paramTypeName"
      }
      val paramsString = paramNameWithType.joinToString(", ")
      val returnType = this.returnType()

      "($paramsString) => $returnType"
    }

    else -> throw IllegalArgumentException("Invalid type ref: $this")
  }
}

/**
 * Returns the return type for this function
 */
fun Node.TypeRef.Func.returnType(): String {
  val ref = this.type.ref

  if (ref.isUnit()) {
    return "void"
  }

  return if (ref.isNullable()) {
    "${ref.name()} | null"
  } else {
    ref.name()
  }
}

/**
 * Returns the return type for this function.
 */
fun Node.Decl.Func.returnType(): String {
  val ref = this.type?.ref ?: return "void"

  val typeName = ref.name()

  if (ref.isNullable()) {
    return "$typeName | null"
  }

  return typeName
}
