# Getting Started

## Requirements
For this example no requirements are needed. It shows how you can run MeisterCharts directly on pure
HTML and JavasScript without any Package Manager and bundling tools.

## Download and Install MeisterCharts

### option 1: Installation with our CDN
```HTML
<script src="https://cdn.skypack.dev/@meistercharts/meistercharts@1.31.0"></script>
```

### option 2: Load MeisterCharts from the CDN as ECMAScript modules

You can import ES Modules directly into your Browser without needing any bundling tools.
```HTML
<script type="module">
  import meistercharts from "https://cdn.skypack.dev/@meistercharts/meistercharts@1.31.0";
  meistercharts.createTimeLineChartFromId('timeLineChart');
</script>
```

## Your First Chart
With MeisterCharts included in your webpage you are ready to create your first chart.

Add a div in your webpage.
```html
<div id="timeLineChart" style="width: 500px; height: 200px;"></div>
```

The ID of the div is accessed by the ``createTimeLineChartFromId`` and the
chart will be created in this container.

If you are facing any problems you can clone this example project
or just try this code snippet:

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>MeisterCharts Demo</title>
  <link rel="stylesheet" href="style.css"/>
</head>
<body>
<div class="example">
  <h1>MeisterCharts minimal example:</h1>

  <div id="timeLineChart" style="width: 500px; height: 200px;">
    <h3>TimeLineChart</h3>
  </div>

  <script type="module">
    import meistercharts from "https://cdn.skypack.dev/@meistercharts/meistercharts@1.31.0";

    meistercharts.createTimeLineChartFromId('timeLineChart');
  </script>

</div>
</body>
</html>
```



