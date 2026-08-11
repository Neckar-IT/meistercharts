The files in this package are copied from the korlibs data structures library:

https://github.com/korlibs/korlibs — module `korlibs-datastructure`, package `korlibs.datastructure`

The import rewrites that package to `it.neckar.open.collections`.

The original repository `https://github.com/korlibs/kds` with the package `com.soywiz.kds` is
archived (it redirects to `soywiz-archive/kds`); the data structures live in the `korlibs`
monorepo now.

To update the files use the script `update-kds-classes.kts`. It clones the previous repository
name `korge-korlibs`, which GitHub redirects to `korlibs`, and reads the sources from
`korlibs-datastructure/src/korlibs/datastructure/`. Upstream has moved them to
`korlibs-datastructure/src/commonMain/korlibs/datastructure/`, so that path has to be adjusted
before the script runs.

The files are licensed under Apache or MIT — see `KDS-LICENSE-APACHE` and `KDS-LICENSE-MIT`
next to this file.
