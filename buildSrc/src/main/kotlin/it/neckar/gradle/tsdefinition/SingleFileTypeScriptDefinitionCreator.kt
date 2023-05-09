package it.neckar.gradle.tsdefinition

import it.neckar.gradle.kastree.FixedKastreeParser
import it.neckar.gradle.kastree.isAbstract
import it.neckar.gradle.kastree.isPrivate
import it.neckar.gradle.kastree.isProtected
import com.google.common.base.Strings
import kastree.ast.Node
import kastree.ast.psi.Converter
import java.io.InputStream
import java.nio.charset.StandardCharsets

/**
 * Creates the type definition for a single file
 */
class SingleFileTypeScriptDefinitionCreator(
  /**
   * The Kotlin source code
   */
  val kotlinSource: String
) {
  constructor(
    /**
     * The Kotlin source code
     */
    kotlinSource: InputStream
  ) : this(kotlinSource.readBytes().toString(StandardCharsets.UTF_8))


  /**
   * The extras map that contains the comments / whitespaces etc
   */
  val extrasMap: Converter.WithExtras = Converter.WithExtras()

  /**
   * The parsed Kotlin file
   */
  val kotlinFile: Node.File = FixedKastreeParser(extrasMap).parseFile(kotlinSource)

  /**
   * The current level of indentation.
   * For each level, spaces are added
   */
  private var indentationLevel = 0

  /**
   * Executes the code with increased indentation
   */
  private fun indented(action: () -> Unit) {
    indentationLevel++
    try {
      action()
    } finally {
      indentationLevel--
    }
  }

  fun create(): String {
    val builder = StringBuilder()

    /**
     * Iterate over all top level declarations (interfaces + external functions)
     */
    kotlinFile.decls.forEach { declaration: Node.Decl ->
      when (declaration) {
        //Append for interface/class
        is Node.Decl.Structured -> {
          declaration.convert(builder)
        }
        is Node.Decl.Func -> {
          declaration.convertFunction(builder, true)
        }
        is Node.Decl.Property -> {
          if (declaration.isPrivate()) {
            return@forEach
          }

          throw UnsupportedOperationException("Not implemented yet: ${declaration::class} - $declaration")
        }

        else -> {
          throw UnsupportedOperationException("Not implemented yet: ${declaration::class}")
        }
      }
    }

    return builder.toString()
  }

  /**
   * Appends the declaration for an enum
   */
  private fun Node.Decl.Structured.convertEnum(builder: StringBuilder) {
    check(this.form == Node.Decl.Structured.Form.ENUM_CLASS) {
      "expected ENUM_CLASS but was ${this.form}"
    }

    //Apply the documentation if there is one
    doc()?.let {
      builder.appendLine()
      builder.appendIndentedln(it)
    }

    builder.appendIndentedln("type ${mapKotlinType2ts(name)} =")

    indented {
      builder.appendLine(members
        .filterIsInstance<Node.Decl.EnumEntry>()
        .joinToString(" |\n") {
          "${indentedDoc(it.doc())}${indentation()}\"${it.name}\""
        })
    }
  }

  private fun indentedDoc(doc: String?): String {
    if (doc == null) {
      return ""
    }
    return "\n${indentation()}$doc\n"
  }

  /**
   * Appends the declaration for a class/interface
   */
  private fun Node.Decl.Structured.convertClass(builder: StringBuilder) {
    check(this.form == Node.Decl.Structured.Form.CLASS || this.form == Node.Decl.Structured.Form.INTERFACE) { "Required Class or interface but was <${this.form}>" }

    //Apply the documentation if there is one
    doc()?.let {
      builder.appendLine()
      builder.appendIndentedln(it)
    }

    //The top level declaration

    //Add the extends method
    val extendsTypes = parents.flatMap {
      when (it) {
        is Node.Decl.Structured.Parent.Type -> {
          it.type.pieces
        }
        is Node.Decl.Structured.Parent.CallConstructor -> {
          it.type.pieces
        }
        else -> {
          listOf()
        }
      }
    }.map {
      it.name
    }.joinToString(", ") {
      it
    }

    val extendsStatement =
      if (extendsTypes.isNotBlank()) {
        "extends $extendsTypes "
      } else {
        ""
      }

    builder.appendIndentedln("interface ${mapKotlinType2ts(name)} $extendsStatement{")

    indented {
      members.forEach {
        when (it) {
          is Node.Decl.Property -> {
            it.convertProperty(builder)
          }

          /**
           * Add the functions
           */
          is Node.Decl.Func -> {
            it.convertFunction(builder, false)
          }

          //Ignore the init block
          is Node.Decl.Init -> return@forEach

          is Node.Decl.Structured -> {
            if (it.form == Node.Decl.Structured.Form.COMPANION_OBJECT) {
              //We ignore companion objects for now
              return@forEach
            }

            throw UnsupportedOperationException("Unsupported: ${it.name} - ${it.form}")
          }

          else -> {
            throw UnsupportedOperationException("Unsupported: ${it::class}")
          }
        }
      }
    }

    //Close the interface
    builder.appendIndentedln("}")
  }

  /**
   * Appends a declaration
   */
  private fun Node.Decl.Structured.convert(builder: StringBuilder) {
    when (this.form) {
      Node.Decl.Structured.Form.CLASS -> convertClass(builder)
      Node.Decl.Structured.Form.ENUM_CLASS -> convertEnum(builder)
      Node.Decl.Structured.Form.INTERFACE -> convertClass(builder)
      else -> throw UnsupportedOperationException("Only Interfaces and Classes supported but was <${this.form}> - ${this.name}")
    }
  }

  /**
   * Converts a property
   */
  private fun Node.Decl.Property.convertProperty(builder: StringBuilder) {
    if (isPrivate()) {
      return
    }

    val first = this.vars.firstOrNull() ?: throw UnsupportedOperationException("no vars")

    doc()?.let {
      builder.appendLine()
      builder.appendIndentedln(it)
    } ?: builder.appendLine()
    val isNullableType = first.isNullable()
    val nullableSuffix = getNullableTypeSuffix(isNullableType)
    val optionalMarker = getOptionalMarker(isNullableType)
    builder.appendIndentedln("${first.name}$optionalMarker: ${first.type?.ref.name()}$nullableSuffix;")
  }


  /**
   * Converts the function
   */
  private fun Node.Decl.Func.convertFunction(builder: StringBuilder, topLevel: Boolean) {
    if (mods.isPrivate() || mods.isProtected()) {
      //Skip private/protected functions
      return
    }

    //Skip deprecated methods
    this.anns.forEach {
      it.anns.forEach {
        if (it.names.contains("Deprecated")) {
          return
        }
      }
    }

    if (mods.isAbstract()) {
      //Skip abstract methods
      return
    }

    doc()?.let {
      builder.appendLine()
      builder.appendIndentedln(it)
    } ?: builder.appendLine()

    if (topLevel) {
      builder.append("export function ")
    }

    builder.appendIndented("$name(")

    params.joinToString(", ") {
      val nullableSuffix = getNullableTypeSuffix(it.isNullable())

      "${it.name}: ${it.type?.ref.name()}$nullableSuffix"
    }.let { builder.append(it) }

    val returnType = this.returnType()
    builder.appendLine("): $returnType;")
  }

  /**
   * Returns ' | null' if nullable. The value must be appended to the return type of the type
   */
  private fun getNullableTypeSuffix(isNullableType: Boolean) = if (isNullableType) " | null" else ""

  /**
   * Returns '?' if nullable. The value must be appended to the property name to indicate that it's an optional property
   */
  private fun getOptionalMarker(isNullableType: Boolean) = if (isNullableType) "?" else ""

  /**
   * Returns the documentation or an empty string for this node.
   */
  private fun Node.doc(): String? {
    return extrasMap.docComment(this)?.text?.trim()
  }

  /**
   * Appends the content but fixes the indentation
   */
  @Suppress("SpellCheckingInspection")
  private fun StringBuilder.appendIndentedln(content: String) {
    appendIndented(content)
    appendLine()
  }

  private fun StringBuilder.appendIndented(content: String) {
    append(indentation())
    append(content)
  }

  /**
   * Returns the current indentation
   */
  private fun indentation(): String {
    return Strings.repeat("  ", indentationLevel)
  }
}
