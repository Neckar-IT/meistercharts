# @meistercharts/meistercharts-react

Wrapper around the MeisterCharts library for React.

## Installation

The package can be installed from npmjs(**TODO**: link package).

#### npm

```
npm install @meistercharts/meistercharts-react
```

#### yarn

```
yarn add @meistercharts/meistercharts-react
```

#### pnpm

```
pnpm add  @meistercharts/meistercharts-react
```

## Usage

To create a Timeline Chart with sample data, insert this code in a React Component.

```JSX
import { TimeLineChart, useTimeLineChartController } from "@meistercharts/meistercharts-react"

export function Chart() {
   const controller = useTimeLineChartController();

   useEffect(() => {
      controller.setUpDemo();
      controller.play();
   })

   return <TimeLineChart controller={controller} />
}
```

This will render the following chart:

**TODO**: insert screenshot
