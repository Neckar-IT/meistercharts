import it.neckar.projects.Projects

description = """Meistercharts React"""


tasks.named("pnpmRunBuild") {
  // A break in meistercharts-easy-api fails this build, although the package comes from the catalog.
  dependsOn(Projects.open.meistercharts.meisterchartsApi.meisterchartsEasyApi.buildTask)
}

// `storybook` and its `run` alias come from the pnpm configuration, with this worktree's port wired in.
