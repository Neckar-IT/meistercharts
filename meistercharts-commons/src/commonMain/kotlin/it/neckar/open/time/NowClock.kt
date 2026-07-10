package it.neckar.open.time

import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A [kotlin.time.Clock] backed by the repo-wide [now] (and thus the swappable [nowProvider]).
 *
 * Production default when a `context(clock: Clock)` is required: it keeps the existing global
 * time source and its test override working for code that has not migrated to a context parameter.
 */
object NowClock : Clock {
  override fun now(): Instant = it.neckar.open.time.now()
}
