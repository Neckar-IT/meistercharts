## Build and Deploy the Library

This project is part of the pnpm workspace.
`@meistercharts/meistercharts` comes from the npm registry, in the version the `catalog:` section of `pnpm-workspace.yaml` names.

### Setup

Install the package-manager pnpm globally:

```
npm install -g pnpm
```

Install the dependencies:

```
pnpm install
```

### Build

- Run `pnpm build` to build the library.
The build output will be placed into `./dist`
- Run `pnpm test` to run all tests
- Run `pnpm lint` to check the code

### Publish

- Run `pnpm publish --access public` to publish the library to npmjs.
(A new publish will only work if the version number in `package.json` is increased.)

The script `pnpm push` will run all the steps above.

## Run Storybook

Run

```
./gradlew :internal:open:meistercharts:meistercharts-react:storybook
```

to build meistercharts-easy-api and start Storybook.

`pnpm storybook` starts Storybook alone.
