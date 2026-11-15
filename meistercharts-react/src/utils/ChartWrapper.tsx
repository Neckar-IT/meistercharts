import type * as meistercharts from "@meistercharts/meistercharts";
import { useEffect, useRef } from "react";
import styles from "../styles/meistercharts.module.css";
import { ChartNotInitializedError, ContainerNotInitializedError } from "../utils/CustomErrors";

/**
 * A chart that is configured through a single `setConfiguration` call.
 * Charts of the older data/style API reach this shape through `LegacyChartWrapper`.
 */
export interface Chart<Config> extends meistercharts.MeisterChartsApi {
  setConfiguration(config: Partial<Config>): void;
}

export interface ChartWrapperProps<Config> {
  /**
   * Receives the chart once it exists. A controller drives exactly one chart:
   * passing the same instance to a second wrapper makes the second chart win.
   */
  controller?: ChartWrapperController<Chart<Config>> | undefined;
  /**
   * Called once per mount. Later identities are ignored, so an arrow function
   * recreated on every render is safe here.
   */
  createChart: () => Chart<Config>;
  /**
   * Pushed into the chart whenever its object identity changes, not on every render.
   * An object literal built inline reconfigures the chart on every render.
   */
  configuration?: Config | undefined;
  style?: React.CSSProperties | undefined;
  className?: string | undefined;
}

/**
 * Ties the imperative lifecycle of a MeisterCharts chart to a React component: the chart is created
 * on mount and disposed on unmount, configuration changes are pushed into the existing chart.
 */
export function ChartWrapper<Config>({
  className,
  configuration,
  createChart,
  style,
  controller,
}: ChartWrapperProps<Config>) {
  const chartRef = useRef<Chart<Config> | null>(null);
  const chartContainerRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    chartRef.current ??= createChart();

    if (chartContainerRef.current === null) throw new ContainerNotInitializedError();
    chartContainerRef.current.appendChild(chartRef.current.holder);
    // absolute positioning lets the chart grow and shrink with the container
    chartRef.current.holder.style.position = "absolute";
    chartRef.current.holder.style.inset = "0";

    return () => {
      chartRef.current?.dispose();
      chartRef.current = null;
    };
    // createChart is left out on purpose: the chart lives from mount to unmount, a new callback
    // identity must not tear it down and rebuild it
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (chartRef.current === null) throw new ChartNotInitializedError();
    controller?.setChart(chartRef.current);
  }, [controller]);

  useEffect(() => {
    if (chartRef.current === null) throw new ChartNotInitializedError();
    chartRef.current.setConfiguration(configuration ?? {});
  }, [configuration]);

  const customClassName = className ? ` ${className}` : "";

  return (
    <div
      ref={chartContainerRef}
      className={`${styles.meisterchartsReact}${customClassName}`}
      data-testid="meistercharts-react-container"
      style={style}
    />
  );
}

/**
 * Reaches the chart of a `ChartWrapper` from outside the render.
 *
 * Its functions must not be called during rendering but e.g. inside a useEffect-hook or
 * click-handler: before the wrapper has mounted no chart is set and every accessor throws.
 */
export class ChartWrapperController<T extends meistercharts.MeisterChartsApi> {
  private chart: T | null = null;

  /**
   * Called by the wrapper. A chart with a different id replaces the previous one and is only
   * reported as a warning — a controller is meant to drive a single chart.
   */
  public setChart(chart: T) {
    if (this.chart != null) {
      if (chart.chartId !== this.chart.chartId) {
        console.warn(
          `reassignment of different chart. Current: ${this.chart.chartId.toFixed(0)} New: ${chart.chartId.toFixed(0)}`,
        );
      }
    }

    this.chart = chart;
  }

  /**
   * Never returns null: without a mounted chart this throws a `ChartNotInitializedError`.
   */
  public getChart(): T {
    if (this.chart === null) throw new ChartNotInitializedError();
    return this.chart;
  }

  /**
   * The same guard as `getChart` for callers that only need the chart to exist.
   */
  public ensureChartIsSet() {
    if (this.chart === null) throw new ChartNotInitializedError();
  }
}
