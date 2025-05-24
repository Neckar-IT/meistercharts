package it.neckar.open.kotlin.lang

/**
 * Realization of this interface will contain at least one not null - [value1] or [value2]
 */
data class Either<T1, T2>(
  val value1: T1?,
  val value2: T2?,
) {
  init {
    require(value1 != null || value2 != null) { "Either must contain one value, but contains two" }
    require(value1 == null || value2 == null) { "Either must contain one value, but does not contain any" }
  }

  /**
   * Will call [block] in case when [first] is not null
   */
  inline fun onFirst(block: (T1) -> Unit): Either<T1, T2> {
    value1?.let {
      block(it)
    }

    return this
  }

  /**
   * Will call [block] in case when [second] is not null
   */
  inline fun onSecond(block: (T2) -> Unit): Either<T1, T2> {
    value2?.let {
      block(it)
    }

    return this
  }

  /**
   * @return Result of [block] if this contains a first value
   */
  inline fun <R> mapOnFirst(block: (T1) -> R): R? {
    return value1?.let {
      block(it)
    }
  }

  /**
   * @return Result of [block] if this contains a second value
   */
  inline fun <R> mapOnSecond(block: (T2) -> R): R? {
    return value2?.let {
      block(it)
    }
  }
}


