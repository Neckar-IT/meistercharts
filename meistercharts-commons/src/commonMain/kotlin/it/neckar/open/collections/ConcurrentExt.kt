package it.neckar.open.collections


/**
 * Creates a synchronized map.
 *
 * ATTENTION: Does *not* create a new instance for JS
 */
expect fun <K, V> MutableMap<K, V>.synchronizedIfNecessary(): MutableMap<K, V>
