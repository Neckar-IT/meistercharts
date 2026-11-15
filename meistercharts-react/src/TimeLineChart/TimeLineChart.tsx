/**
 * Imports
 */

import * as meistercharts from "@meistercharts/meistercharts";
import type { CSSProperties } from "react";
import { memo, useEffect, useRef } from "react";
import styles from "../styles/meistercharts.module.css";
import { ChartNotInitializedError } from "../utils/CustomErrors";
import { type TimeLineChartController } from "./useTimeLineChartController";

/**
 * Types
 */

export interface TimeLineChartConfiguration {
  decimalDataSeries?: meistercharts.DecimalDataSeries[];
  enumDataSeries?: meistercharts.EnumDataSeries[];
  style?: meistercharts.TimeLineChartStyle;
  historySettings?: meistercharts.HistorySettings;
}

interface Props {
  /**
   * The controller of the chart.
   * Create a controller with the `useTimeLineChartController` hook.
   */
  controller?: TimeLineChartController | undefined;
  /**
   * The configuration of the chart.
   */
  configuration?: TimeLineChartConfiguration | undefined;
  /**
   * Specifies the inline style of the chart container.
   */
  style?: CSSProperties | undefined;

  /**
   * Specifies the i18n-configuration of the chart. If not set, the default i18n-configuration is used.
   */
  i18nConfiguration?: meistercharts.I18nConfiguration | undefined;

  /**
   * Specifies the theme of the chart. If not set, the default theme is used.
   */
  theme?: meistercharts.ThemeId | undefined;

  /**
   * Specifies the CSS classes that are applied to the chart container.
   */
  className?: string | undefined;
}

type TimeLineChartControllerWithSetChart = TimeLineChartController & {
  setChart: (chart: meistercharts.TimeLineChart) => void;
};

/**
 * TimeLineChart Component
 */
export const TimeLineChart = memo(
  ({ className, configuration, controller, style, i18nConfiguration, theme }: Props) => {
    const chartRef = useRef<meistercharts.TimeLineChart | null>(null);
    const chartContainerRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
      console.assert(chartRef.current === null);
      chartRef.current = meistercharts.createTimeLineChart();

      console.assert(
        chartContainerRef.current !== null,
        "The container for the chart was used before it was initialized.",
      );
      chartContainerRef.current!.appendChild(chartRef.current.holder);

      // absolute positioning lets the chart grow and shrink with the container
      chartRef.current.holder.style.position = "absolute";
      chartRef.current.holder.style.inset = "0";

      return () => {
        chartRef.current?.dispose();
        chartRef.current = null;
      };
    }, []);

    useEffect(() => {
      console.assert(chartRef.current !== null);
      (controller as TimeLineChartControllerWithSetChart)?.setChart(chartRef.current!);
    }, [controller]);

    useEffect(() => {
      if (chartRef.current === null) throw new ChartNotInitializedError();

      chartRef.current.setI18nConfiguration(i18nConfiguration);
    }, [i18nConfiguration]);

    useEffect(() => {
      if (chartRef.current === null) throw new ChartNotInitializedError();
      chartRef.current.setTheme(theme);
    }, [theme]);

    useEffect(() => {
      if (chartRef.current === null) throw new ChartNotInitializedError();
      chartRef.current.setStyle({ ...configuration?.style });
    }, [configuration?.style]);

    useEffect(() => {
      if (chartRef.current === null) throw new ChartNotInitializedError();
      chartRef.current.setData({
        historySettings: configuration?.historySettings,
      });
    }, [configuration?.historySettings]);

    useEffect(() => {
      if (chartRef.current === null) throw new ChartNotInitializedError();
      chartRef.current.setDataSeries(configuration?.decimalDataSeries ?? [], configuration?.enumDataSeries ?? []);
    }, [configuration?.decimalDataSeries, configuration?.enumDataSeries]);

    const customClassName = className ? ` ${className}` : "";

    return (
      <div
        ref={chartContainerRef}
        className={`${styles.meisterchartsReact}${customClassName}`}
        data-testid="meistercharts-react-container"
        style={style}
      />
    );
  },
);
TimeLineChart.displayName = "TimeLineChart";
