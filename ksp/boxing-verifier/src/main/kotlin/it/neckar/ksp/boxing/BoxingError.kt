package it.neckar.ksp.boxing

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Location
import com.google.devtools.ksp.symbol.NonExistLocation
import it.neckar.ksp.format
import it.neckar.ksp.fqName

/**
 * Represents a detected invalid boxing
 */
data class BoxingError(
  /**
   * Describes the location (file path + line number)
   */
  val location: Location,

  /**
   * The class name - if there is one
   */
  val className: String? = null,

  /**
   * The function name
   */
  val functionName: String? = null,
  /**
   * The offending parameter - if there is one.
   */
  val parameterName: String? = null,

  /**
   * The type of the parameter or return type - depending on the [typeLocation]
   */
  val type: String? = null,

  /**
   * Where the error is located
   */
  val typeLocation: BoxingErrorTypeLocation? = null,

  /**
   * The additional message with more information
   */
  val additionalMessage: String,
) {

  init {
    require(additionalMessage.isNotEmpty()) {
      "Message must not be blank"
    }
  }

  fun format(): String {
    return when (typeLocation) {
      BoxingErrorTypeLocation.ReturnType -> "Possible boxing for return value in $functionName: $type - $additionalMessage @ ${location.format()}: "
      BoxingErrorTypeLocation.Parameter -> "Possible boxing for parameter ($parameterName: $type) in $functionName - $additionalMessage @ ${location.format()}: "
      null -> "Unknown error in $functionName @ ${location.format()}: $additionalMessage"
    }
  }

  /**
   * Adds the values - if they are missing
   */
  fun fillIfMissing(
    /**
     * Is only set if the current location is unknown
     */
    location: Location,
    className: String? = null,
    functionName: String? = null,
    parameterName: String? = null,
    type: String? = null,
    typeLocation: BoxingErrorTypeLocation? = null,
  ): BoxingError {

    val newLocation = when (this.location) {
      is NonExistLocation -> location
      else -> this.location
    }

    return copy(
      location = newLocation,
      className = this.className ?: className,
      functionName = this.functionName ?: functionName,
      parameterName = this.parameterName ?: parameterName,
      type = this.type ?: type,
      typeLocation = this.typeLocation ?: typeLocation,
    )
  }

  companion object {
    /**
     * Create a new instance using the [KSType]
     */
    operator fun invoke(
      location: Location,
      type: KSType,
      message: String,
    ): BoxingError {
      return BoxingError(
        location = location,
        type = type.declaration.fqName(),
        additionalMessage = message
      )
    }
  }

  /**
   * The type of boxing error type
   */
  enum class BoxingErrorTypeLocation {
    ReturnType,
    Parameter,
  }
}
