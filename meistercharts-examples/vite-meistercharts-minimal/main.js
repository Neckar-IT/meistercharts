import './style.css';
import * as meisterCharts from '@meistercharts/meistercharts';

let chart = meisterCharts.createTimeLineChartFromId('timeLineChart');

// create the first sample data
chart.setUpDemo();
