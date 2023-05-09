package it.neckar.open.kotlin.lang

actual inline fun <T> Any?.fastCastTo(): T {
  return this as T
}
