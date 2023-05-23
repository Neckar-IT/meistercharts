package it.neckar.open.kotlin.lang

import kotlin.random.Random

/**
 * Global random variable
 *
 * The random generator that is used to generate random values.
 * Can be replaced with a seeded instance for tests
 */
var random: Random = Random.Default

