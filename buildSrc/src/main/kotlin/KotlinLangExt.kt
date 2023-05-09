/**
 * Executes the block if this is null
 */
fun <T> T?.ifNull(block: () -> Unit): T? {
  if (this == null) {
    block()
  }

  return this
}
