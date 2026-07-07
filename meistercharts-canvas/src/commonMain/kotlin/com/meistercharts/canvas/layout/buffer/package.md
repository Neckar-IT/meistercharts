# Package com.meistercharts.canvas.layout.buffer

Reusable, pooled layout scratch buffers. NOT caches — no memoization, no hit/miss.
Each buffer is reused every paint pass (filled in `layout()`, read in `paint()`) to keep
the hot path allocation-free. Grow-only; see [LayoutVariableWithSize].
