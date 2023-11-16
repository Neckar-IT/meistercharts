package it.neckar.ksp.ts

import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import it.neckar.ksp.format
import it.neckar.ksp.isDeprecated
import it.neckar.ksp.isClassProperty
import java.io.BufferedWriter

/**
 * Creates the ts file declarations with constants that can be used in the TypeScript code
 */
class ExportTypescriptFileVisitor(val writer: BufferedWriter, val logger: KSPLogger) : KSTopDownVisitor<GeneratingContext, Unit>() {
  override fun defaultHandler(node: KSNode, data: GeneratingContext) {
  }

  @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
  override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, generatingContext: GeneratingContext) {
    if (classDeclaration.isCompanionObject) {
      return  //skip companion objects
    }

    if (classDeclaration.isDeprecated()) {
      //Skip deprecated functions
      return
    }

    if (classDeclaration.isInternal()) {
      return
    }
    if (classDeclaration.isPrivate()) {
      return
    }

    when (classDeclaration.classKind) {
      ClassKind.CLASS, ClassKind.INTERFACE -> {
        //noop
      }

      ClassKind.ENUM_CLASS -> {
        //noop
      }

      ClassKind.ENUM_ENTRY -> {
        //noop
      }
      //else -> throw IllegalArgumentException("Unsupported class kind ${classDeclaration.classKind}")
      ClassKind.OBJECT -> {
        exportObject(classDeclaration, generatingContext)
      }

      ClassKind.ANNOTATION_CLASS -> {
        return // do nothing
      }
    }
  }

  private fun exportObject(classDeclaration: KSClassDeclaration, generatingContext: GeneratingContext) {
    logger.info("Export object ${classDeclaration.simpleName.asString()}")

    val indentation = generatingContext.indentation()
    val objectNameString = classDeclaration.simpleName.asString()

    writer.appendLine()

    DocumentationSupport.createKotlinDoc(classDeclaration.docString, indentation)?.let {
      writer.appendLine(it)
    }

    writer.appendLine("${indentation}//Kotlin object: ${classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()}")

    if (generatingContext.isTopLevel()) {
      writer.appendLine("${indentation}export const $objectNameString = {")
    } else {
      writer.appendLine("${indentation}$objectNameString : {")
    }

    generatingContext.withNewClassName(objectNameString) {
      generatingContext.withIncreasedIndentationLevel {
        super.visitClassDeclaration(classDeclaration, generatingContext)
      }
    }

    writer.append("$indentation}")
    if (generatingContext.isTopLevel()) {
      writer.appendLine()
    } else {
      writer.appendLine(",")
    }
  }

  override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: GeneratingContext) {
    //Do *NOT* call the super method, we are not interested in the body of the function
    if (false) {
      super.visitFunctionDeclaration(function, data)
    }
  }

  override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: GeneratingContext) {
    try {
      val simpleName = property.simpleName.asString()
      val typeResolved = property.type.resolve()
      logger.info("Visiting property $simpleName with type ${typeResolved.declaration.simpleName.asString()}")

      if (property.isDeprecated()) {
        //Skip deprecated functions
        return
      }

      if (property.isPrivate() || property.isInternal()) {
        //Skip private, internal functions
        return
      }

      if (property.isProtected()) {
        //Skip protected
        return
      }

      if (property.isClassProperty().not()) {
        return
      }

      require(property.isClassProperty()) {
      "Only for class parent declaration: ${property.parentDeclaration}"
    }

      //Empty line before property
      writer.appendLine()

      val indentation = data.indentation()

      DocumentationSupport.createKotlinDoc(property.docString, indentation)?.let {
        writer.appendLine(it)
      }

      val typeName: String = TypeScriptTypeSupport.toTypescriptType(typeResolved, logger)

      require(data.isTopLevel().not()) {
        "Top level properties are not supported: $typeName - ${property.qualifiedName?.asString() ?: property.simpleName.asString()} @ ${property.location.format()}"
      }

      if (property.hasBackingField) {
        val pc = property as com.google.devtools.ksp.symbol.impl.kotlin.KSPropertyDeclarationImpl

        val initializerText = pc.ktProperty.initializer?.text ?: "???"

        writer.appendLine("""${indentation}${simpleName}: $initializerText,""")
      } else {
        writer.appendLine("""${indentation}${simpleName}: "???", """)
      }
    } catch (e: Exception) {
      throw RuntimeException("Error while processing property ${property.qualifiedName?.asString() ?: property.simpleName.asString()} @ ${property.location.format()}:\n${e.message}", e)
    }
  }
}
