@file:OptIn(KspExperimental::class)

package it.neckar.ksp.boxing

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.innerArguments
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import it.neckar.ksp.findContainingClass
import it.neckar.ksp.findSuperFunction
import it.neckar.ksp.findSuperParameter
import it.neckar.ksp.format
import it.neckar.ksp.fqName
import it.neckar.ksp.getFunctionsWithAnnotationOrAnnotatedParameters
import it.neckar.ksp.isAnnotatedAnywhere
import it.neckar.ksp.isPrimitive
import it.neckar.ksp.isPrimitiveArray
import it.neckar.ksp.isPrimitiveCollection
import it.neckar.ksp.isReturnTypeAnnotated
import it.neckar.open.annotations.NotBoxed

// private val out = File("/tmp/out.log").outputStream().bufferedWriter()

class BoxingVerifierProcessor(val codeGenerator: CodeGenerator, val logger: KSPLogger) : SymbolProcessor {

  override fun process(resolver: Resolver): List<KSAnnotated> {
    resolver.getFunctionsWithAnnotationOrAnnotatedParameters(NotBoxed::class).forEach {
      logger.info("Verifying annotated function $it @ ${it.location.format()}")
      val error = resolver.findBoxingErrors(it, logger)
      if (error != null) {
        logger.error(error.format(), it)
      }
    }

    return emptyList()
  }

  /**
   * Throws an exception if the function is boxing
   */
  private fun Resolver.findBoxingErrors(functionDeclaration: KSFunctionDeclaration, logger: KSPLogger): BoxingError? {
    val superFunction = findSuperFunction(functionDeclaration)

    //TODO check *all* parameters, too, if the method itself is annotated

    //Check the return type first
    if (functionDeclaration.isReturnTypeAnnotated(NotBoxed::class)) {
      val returnType = requireNotNull(functionDeclaration.returnType)
      val returnTypeBoxingError = returnType.detectBoxingErrors(superFunction?.returnType, logger)

      if (returnTypeBoxingError != null) {
        return returnTypeBoxingError.fillIfMissing(
          location = functionDeclaration.location,
          functionName = functionDeclaration.fqName(),
          typeLocation = BoxingError.BoxingErrorTypeLocation.ReturnType,
          className = functionDeclaration.findContainingClass()?.fqName(),
        )
      }
    }

    //Check the parameters (if annotated)
    functionDeclaration.parameters.forEach { parameter ->
      logger.info("\tParameter: ${parameter.name?.asString()} with type ${parameter.type.resolve().toString()}")

      if (parameter.isAnnotatedAnywhere(NotBoxed::class)) {
        val parameterBoxingError = parameter.type.detectBoxingErrors(findSuperParameter(functionDeclaration, superFunction, parameter), logger)
        if (parameterBoxingError != null) {
          return parameterBoxingError.fillIfMissing(
            location = functionDeclaration.location,
            functionName = functionDeclaration.fqName(),
            typeLocation = BoxingError.BoxingErrorTypeLocation.Parameter,
            parameterName = parameter.name?.asString(),
            className = functionDeclaration.findContainingClass()?.fqName(),
          )
        }
      }
    }

    return null //no error found
  }
}

/**
 * Returns the detected boxing errors - if there are any
 */
fun KSTypeReference.detectBoxingErrors(superType: KSTypeReference?, logger: KSPLogger): BoxingError? {
  val resolvedType: KSType = this.resolve()
  if (resolvedType.isMarkedNullable) {
    //If nullable, boxing is unavoidable
    return BoxingError(
      this.location,
      type = resolvedType,
      message = "must not be nullable but was [${resolvedType.declaration.fqName()}]"
    )
  }

  val resolvedDeclaration = resolvedType.declaration

  //check for the type itself
  when {
    resolvedType.isFunctionType -> return resolvedType.detectBoxingErrorsForFunction(logger)?.copy(
      location = location,
      type = resolvedType.declaration.fqName(),
    )

    resolvedDeclaration.isPrimitive() -> {} //good
    resolvedDeclaration.isPrimitiveArray() -> {} //good
    resolvedDeclaration.isPrimitiveCollection() -> {} //good
    else -> {
      return BoxingError(
        this.location,
        type = resolvedType,
        message = "is not primitive but [${resolvedType.declaration.fqName()}]"
      )
    }
  }

  if (superType != null) {
    //Check for the super type
    return superType.detectBoxingErrors(null, logger)
  }

  return null //no problems found
}

/**
 * Detects possible boxing errors for a function type
 */
fun KSType.detectBoxingErrorsForFunction(logger: KSPLogger): BoxingError? {
  require(this.isFunctionType) {
    "Must only be called for function types. But was $this"
  }

  this.innerArguments.forEachIndexed { index, argument ->
    val argumentType = argument.type
    if (argumentType != null && argumentType.isAnnotationPresent(NotBoxed::class)) {
      val boxingError = argumentType.detectBoxingErrors(null, logger)
      if (boxingError != null) {
        return boxingError
          .copy(additionalMessage = "Argument [$index] ${boxingError.additionalMessage}") //enforce the new message
          .fillIfMissing(
            location = argument.location,
          )
      }
    }
  }

  return null
}

