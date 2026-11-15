import type * as meistercharts from "@meistercharts/meistercharts";
import { useMemo } from "react";
import { ChartNotInitializedError } from "../utils/CustomErrors";

/**
 * A controller to access functions of the time-line chart.
 *
 * Functions must not be called during rendering but e.g. inside a useEffect-hook or click-handler.
 */
export interface TimeLineChartController {
  setUpDemo: () => void;
  addSample: (sample: meistercharts.Sample) => void;
  addSamples: (samples: meistercharts.Sample[]) => void;
  clearHistory: () => void;
  resetView: () => void;
  getZoom: () => meistercharts.Zoom;
  modifyZoom: (zoom?: meistercharts.Zoom | null, zoomCenterX?: number | null, zoomCenterY?: number | null) => void;
  play: () => void;
  pause: () => void;
  getVisibleTimeRange: () => meistercharts.TimeRange;
}

let controllerCounter = 0;

class TimeLineChartControllerImpl implements TimeLineChartController {
  private chart: meistercharts.TimeLineChart | null = null;

  readonly id;

  constructor() {
    this.id = controllerCounter++;
  }

  public setChart(chart: meistercharts.TimeLineChart) {
    if (this.chart != null) {
      if (chart.chartId !== this.chart.chartId) {
        throw new Error(
          `reassignment of different chart. Current: ${this.chart.chartId.toFixed(0)} New: ${chart.chartId.toFixed(0)}`,
        );
      }
    }

    // workaround: dependency=pnpm:react:19.3.0 observed-on=2026-10-07 upstream=https://github.com/facebook/react/issues/26315
    // StrictMode mounts the component twice, so an assert on chart reassignment would fire in development.
    this.chart = chart;
  }

  public setUpDemo() {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.setUpDemo();
  }

  public addSample(sample: meistercharts.Sample) {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.addSample(sample);
  }

  public addSamples(samples: meistercharts.Sample[]) {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.addSamples(samples);
  }

  public clearHistory() {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.clearHistory();
  }

  public resetView() {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.resetView();
  }

  public getZoom() {
    if (this.chart === null) throw new ChartNotInitializedError();
    return this.chart.getZoom();
  }

  public modifyZoom(zoom?: meistercharts.Zoom | null, zoomCenterX?: number | null, zoomCenterY?: number | null) {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.modifyZoom(zoom, zoomCenterX, zoomCenterY);
  }

  public play() {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.setData({ play: true });
  }

  public pause() {
    if (this.chart === null) throw new ChartNotInitializedError();
    this.chart.setData({ play: false });
  }

  public getVisibleTimeRange() {
    if (this.chart === null) throw new ChartNotInitializedError();
    return this.chart.getVisibleTimeRange();
  }
}

/**
 * Returns a controller to access functions of the time-line chart.
 */
export function useTimeLineChartController() {
  return useMemo(() => new TimeLineChartControllerImpl(), []);
}
