# Vite Meistercharts Minimal Example

## Getting Started
This is an example that shows how to use MeisterCharts with Vite and NPM Package Manager.

### System Requirements

#### 1. Node.js

Before installing and using the NPM package manager, you will need to have Node.js installed. To see if you already have Node.js installed,
type the following command into your local command line terminal:

```shell
node -v
```
If you see a version number, Node.js is installed.
To install Node.js, follow [this link](https://nodejs.org/en).

### Add MeisterCharts to an existing Vite Project

#### 1. Adding Dependencies
add meistercharts to the dependencies:

```shell
yarn add @meistercharts/meistercharts
```
Your ``package.json`` should now contain the MeisterCharts dependency:

[`package.json`](package.json)

That's all you need to do. Now your project is ready to use MeisterCharts.
The next step is to implement MeisterCharts in your code.

#### Import MeisterCharts and create your first Chart

[`main.js`](main.js)

- Add a new div to your webpage. The ID of the div is accessed by the ``createTimeLineChartFromId`` and the
  chart will be created in this container.

[`index.html`](index.html)

After we created the chart you should now see the following chart on your browser

![](img/timeLineChart.png)


### Run this example project
First you need to install all Dependencies:

```shell
npm install
```

After installing all dependencies you can run the following command:

```shell
npm run dev
```

Now you can open **[http://localhost:5173](http://localhost:5173/)**
