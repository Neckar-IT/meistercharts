package it.neckar.open.time

import it.neckar.open.dispose.Disposable
import kotlin.time.Duration

/**
 * Executes the given callback with a delay on the main thread.
 *
 * Returns a [Disposable] which may be used to cancel the timer
 */
expect fun delay(delay: Duration, callback: () -> Unit): Disposable

/**
 * Repeats the given lambda every [delay] on the main thread
 */
expect fun repeat(delay: Duration, callback: () -> Unit): Disposable
