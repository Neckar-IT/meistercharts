package it.neckar.ksp.ts

/**
 * Context class for generation.
 */
class GeneratingContext(val indentationString: String = "  ") {
  /**
   * The current class (or object) name - if there is one
   */
  private val classNamesStack = mutableListOf<String>()

  fun isTopLevel(): Boolean {
    return classNamesStack.isEmpty()
  }

  fun withNewClassName(newClassName: String, block: () -> Unit) {
    classNamesStack.add(newClassName)
    block()
    classNamesStack.removeLast()
  }

  /**
   * The current indentation level
   */
  private var indentation: Int = 0

  fun withIncreasedIndentationLevel(block: () -> Unit) {
    indentation++
    block()
    indentation--
  }

  fun indentation(): String {
    return indentationString.repeat(indentation)
  }

  /**
   * The current enum entries count
   */
  var enumEntriesCount: Int = 0
    private set

  fun resetEnumEntriesCount() {
    enumEntriesCount = 0
  }

  fun increaseEnumEntriesCount() {
    enumEntriesCount++
  }
}
