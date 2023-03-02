'use strict';

/**
 * MeisterCharts as a React component
 *
 * Pass the chart type as HTML attribute "chart"
 */
class MeisterChartReact extends React.Component {
  constructor(props) {
    super(props);
    this.meisterChartHolderRef = React.createRef();
  }

  componentDidMount() {
    // rendered for the first time
    demosjs.com.meistercharts.demojs.startChartingDemo(this.meisterChartHolderRef.current, this.props.chart);
  }

  componentWillUnmount() {
    // removed from the DOM
    // TODO dispose MeisterCharts instance
  }

  shouldComponentUpdate(nextProps) {
    // tell React to never call render()
    return false;
  }

  render() {
    return (
      <div ref={this.meisterChartHolderRef} style={{width: "100%", height: "100%"}}></div>
    );
  }
}
