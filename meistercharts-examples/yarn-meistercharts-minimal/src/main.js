const meisterCharts = require('@meistercharts/meistercharts/meistercharts-easy-api');
// create a new TimeLineChart
let chart = meisterCharts.createTimeLineChartFromId('timeLineChart');

// create the first sample data
chart.setUpDemo();
