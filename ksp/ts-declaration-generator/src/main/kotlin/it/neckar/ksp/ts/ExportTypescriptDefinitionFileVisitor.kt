@file:Suppress("DuplicatedCode")

package it.neckar.ksp.ts

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.isInternal
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isProtected
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import it.neckar.ksp.format
import it.neckar.ksp.isDeprecated
import it.neckar.ksp.isProperty
import java.io.BufferedWriter

/**
 * Creates the d.ts file declarations
 */
class ExportTypescriptDefinitionFileVisitor(val writer: BufferedWriter, val logger: KSPLogger) : KSTopDownVisitor<GeneratingContext, Unit>() {
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
        //always generate interfaces - even for classes
        exportClassOrInterface(classDeclaration, generatingContext)
      }

      ClassKind.ENUM_CLASS -> {
        exportEnumClass(classDeclaration, generatingContext)
      }

      ClassKind.ENUM_ENTRY -> {
        exportEnumEntry(classDeclaration, generatingContext)
      }

      ClassKind.OBJECT -> {
        //do nothing
      }

      ClassKind.ANNOTATION_CLASS -> {
        return // do nothing
      }
    }
  }

  private fun exportEnumEntry(classDeclaration: KSClassDeclaration, generatingContext: GeneratingContext) {
    logger.info("Export enum entry ${classDeclaration.simpleName}")

    generatingContext.increaseEnumEntriesCount()

    val indentation = generatingContext.indentation()
    val enumEntryName = classDeclaration.simpleName.asString()

    if (generatingContext.enumEntriesCount > 1) {
      writer.appendLine(" |")
    }

    DocumentationSupport.createKotlinDoc(classDeclaration.docString, indentation)?.let {
      writer.appendLine(it)
    }

    writer.append("""$indentation"$enumEntryName"""")
  }

  private fun exportEnumClass(classDeclaration: KSClassDeclaration, generatingContext: GeneratingContext) {
    logger.info("Export enum ${classDeclaration.simpleName}")

    val indentation = generatingContext.indentation()
    val enumNameString = classDeclaration.simpleName.asString()

    //Empty line before class
    writer.appendLine()

    DocumentationSupport.createKotlinDoc(classDeclaration.docString, indentation)?.let {
      writer.appendLine(it)
    }

    writer.appendLine("${indentation}//Kotlin class: ${classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()}")

    writer.appendLine("type $enumNameString = ")

    generatingContext.resetEnumEntriesCount()
    generatingContext.withIncreasedIndentationLevel {
      super.visitClassDeclaration(classDeclaration, generatingContext)
    }

    writer.appendLine("") //final newline
  }

  private fun exportClassOrInterface(classDeclaration: KSClassDeclaration, generatingContext: GeneratingContext) {
    logger.info("Export class/interface ${classDeclaration.simpleName.asString()}")

    val indentation = generatingContext.indentation()
    val classNameString = classDeclaration.simpleName.asString()

    //Empty line before class
    writer.appendLine()

    DocumentationSupport.createKotlinDoc(classDeclaration.docString, indentation)?.let {
      writer.appendLine(it)
    }

    writer.appendLine("${indentation}//Kotlin class: ${classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()}")

    val superTypesString = createSuperTypesString(classDeclaration.superTypes)

    //Export classes and interfaces as "interface"
    writer.appendLine("${indentation}export interface $classNameString $superTypesString{")

    generatingContext.withNewClassName(classNameString) {
      generatingContext.withIncreasedIndentationLevel {
        super.visitClassDeclaration(classDeclaration, generatingContext)
      }
    }

    writer.appendLine("${indentation}}")
  }


  private fun createSuperTypesString(superTypes: Sequence<KSTypeReference>): String {
    val superTypesString = superTypes
      .filter {
        it.resolve().declaration.simpleName.asString() != "Any"
      }
      .map {
        it.resolve().declaration.simpleName.asString()
      }.joinToString(", ")

    return if (superTypesString.isBlank()) {
      ""
    } else {
      "extends $superTypesString "
    }
  }

  override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: GeneratingContext) {
    val name = function.simpleName
    logger.info("Visiting function ${name.asString()}")

    if (function.isDeprecated()) {
      //Skip deprecated functions
      return
    }

    if (function.isConstructor()) {
      //Skip constructors
      return
    }

    if (function.isPrivate() || function.isInternal()) {
      //Skip private, internal functions
      return
    }

    if (function.isProtected()) {
      //Skip protected
      return
    }

    if (function.isAbstract) {
      //Skip abstract functions
      return
    }

    //Empty line before function
    writer.appendLine()

    val indentation = data.indentation()

    DocumentationSupport.createKotlinDoc(function.docString, indentation)?.let {
      writer.appendLine(it)
    }

    //Collect the parameters
    val parameters = function.parameters.map {
      ResolvedParameter(
        parameterName = (it.name?.asString() ?: "unknown") + TypeScriptTypeSupport.getOptionalMarker(it.type.resolve()),
        typeName = TypeScriptTypeSupport.toTypescriptType(it.type.resolve(), logger)
      )
    }

    val parametersString = parameters.joinToString(", ") {
      it.parameterName + ": " + it.typeName
    }

    val functionReturnType = function.returnType

    val returnTypeString = if (functionReturnType != null) {
      TypeScriptTypeSupport.toTypescriptType(functionReturnType.resolve(), logger)
    } else {
      "void"
    }

    if (data.isTopLevel()) {
      writer.appendLine("${indentation}export function ${name.asString()}($parametersString) : $returnTypeString;")
    } else {
      writer.appendLine("${indentation}${name.asString()}($parametersString) : $returnTypeString;")
    }


    //Do *NOT* call the super method, we are not interested in the body of the function
    //super.visitFunctionDeclaration(function, data)
  }


  override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: GeneratingContext) {
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

    require(property.isProperty()) {
      logger.info("Parent declaration: ${property.parentDeclaration}")
      "Parent declaration: ${property.parentDeclaration}"
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

    writer.appendLine("${indentation}${simpleName}${TypeScriptTypeSupport.getOptionalMarker(typeResolved)}: ${typeName};")
  }
}

private data class ResolvedParameter(val parameterName: String, val typeName: String)

