# Package com.meistercharts.canvas.layout.buffer

Reusable, pooled layout scratch buffers. NOT caches — no memoization, no hit/miss.
Each buffer is reused every paint pass (filled in `layout()`, read in `paint()`) to keep
the hot path allocation-free. Grow-only; see [LayoutVariableWithSize].

For when to use these buffers vs. reused algorithm scratch (and when a painter gets its own
layout phase at all), see `internal/open/meistercharts/architecture/painters.md`.
