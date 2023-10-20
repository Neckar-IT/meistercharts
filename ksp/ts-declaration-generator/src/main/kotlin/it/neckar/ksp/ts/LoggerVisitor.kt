package it.neckar.ksp.ts

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSDefNonNullReference
import com.google.devtools.ksp.symbol.KSDynamicReference
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSParenthesizedReference
import com.google.devtools.ksp.symbol.KSPropertyAccessor
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import it.neckar.ksp.format

class LoggerVisitor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : KSTopDownVisitor<Int, Unit>() {
  private val debugFileOut = codeGenerator.createNewFile(Dependencies(false), "it.neckar", "structure", "txt").bufferedWriter()

  private fun log(str: String) {
    debugFileOut.appendLine(str)
    debugFileOut.flush()
  }

  private fun indent(data: Int): String {
    return " ".repeat(data)
  }

  override fun defaultHandler(node: KSNode, data: Int) {
    log("${indent(data)}defaultHandler ${node::class.simpleName} - $node @ ${node.location.format()}")
  }

  override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Int) {
    log("${indent(data)}visitPropertyDeclaration: $property $data")
    super.visitPropertyDeclaration(property, data + 1)
  }

  override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Int) {
    log("${indent(data)}visitPropertyAccessor: $accessor $data")
    super.visitPropertyAccessor(accessor, data + 1)
  }

  override fun visitAnnotated(annotated: KSAnnotated, data: Int) {
    log("${indent(data)}visitPropertyAccessor: $annotated $data")
    super.visitAnnotated(annotated, data + 1)
  }

  override fun visitAnnotation(annotation: KSAnnotation, data: Int) {
    log("${indent(data)}visitAnnotation: $annotation $data")
    super.visitAnnotation(annotation, data + 1)
  }

  override fun visitCallableReference(reference: KSCallableReference, data: Int) {
    log("${indent(data)}visitCallableReference: $reference $data")
    super.visitCallableReference(reference, data + 1)
  }

  override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Int) {
    log("${indent(data)}visitClassDeclaration: $classDeclaration $data")
    super.visitClassDeclaration(classDeclaration, data + 1)
  }

  override fun visitClassifierReference(reference: KSClassifierReference, data: Int) {
    log("${indent(data)}visitClassifierReference: $reference $data")
    super.visitClassifierReference(reference, data + 1)
  }

  override fun visitDeclaration(declaration: KSDeclaration, data: Int) {
    log("${indent(data)}visitDeclaration: $declaration $data")
    super.visitDeclaration(declaration, data + 1)
  }

  override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Int) {
    log("${indent(data)}visitDeclarationContainer: $declarationContainer $data")
    super.visitDeclarationContainer(declarationContainer, data + 1)
  }

  override fun visitDynamicReference(reference: KSDynamicReference, data: Int) {
    log("${indent(data)}visitDynamicReference: $reference $data")
    super.visitDynamicReference(reference, data + 1)
  }

  override fun visitFile(file: KSFile, data: Int) {
    log("${indent(data)}visitFile: $file $data")
    super.visitFile(file, data + 1)
  }

  override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Int) {
    log("${indent(data)}visitFunctionDeclaration: $function $data")
    super.visitFunctionDeclaration(function, data + 1)
  }

  override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Int) {
    log("${indent(data)}visitModifierListOwner: $modifierListOwner $data")
    super.visitModifierListOwner(modifierListOwner, data + 1)
  }

  override fun visitNode(node: KSNode, data: Int) {
    log("${indent(data)}visitNode: $node $data")
    super.visitNode(node, data + 1)
  }

  override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Int) {
    log("${indent(data)}visitParenthesizedReference: $reference $data")
    super.visitParenthesizedReference(reference, data + 1)
  }

  override fun visitPropertyGetter(getter: KSPropertyGetter, data: Int) {
    log("${indent(data)}visitPropertyGetter: $getter $data")
    super.visitPropertyGetter(getter, data + 1)
  }

  override fun visitPropertySetter(setter: KSPropertySetter, data: Int) {
    log("${indent(data)}visitPropertySetter: $setter $data")
    super.visitPropertySetter(setter, data + 1)
  }

  override fun visitReferenceElement(element: KSReferenceElement, data: Int) {
    log("${indent(data)}visitReferenceElement: $element $data")
    super.visitReferenceElement(element, data + 1)
  }

  override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Int) {
    log("${indent(data)}visitTypeAlias: $typeAlias $data")
    super.visitTypeAlias(typeAlias, data + 1)
  }

  override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Int) {
    log("${indent(data)}visitTypeArgument: $typeArgument $data")
    super.visitTypeArgument(typeArgument, data + 1)
  }

  override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Int) {
    log("${indent(data)}visitTypeParameter: $typeParameter $data")
    super.visitTypeParameter(typeParameter, data + 1)
  }

  override fun visitTypeReference(typeReference: KSTypeReference, data: Int) {
    log("${indent(data)}visitTypeReference: $typeReference $data")
    super.visitTypeReference(typeReference, data + 1)
  }

  override fun visitValueArgument(valueArgument: KSValueArgument, data: Int) {
    log("${indent(data)}visitValueArgument: $valueArgument $data")
    super.visitValueArgument(valueArgument, data + 1)
  }

  override fun visitValueParameter(valueParameter: KSValueParameter, data: Int) {
    log("${indent(data)}visitValueParameter: $valueParameter $data")
    super.visitValueParameter(valueParameter, data + 1)
  }

  override fun visitDefNonNullReference(reference: KSDefNonNullReference, data: Int) {
    log("${indent(data)}visitDefNonNullReference: $reference $data")
    super.visitDefNonNullReference(reference, data + 1)
  }
}
