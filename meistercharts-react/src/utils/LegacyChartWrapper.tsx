import type * as meistercharts from "@meistercharts/meistercharts";
import { type Chart, ChartWrapper } from "./ChartWrapper";

/**
 * A chart that is still configured through separate `setData` and `setStyle` calls
 * instead of the single `setConfiguration` of `Chart`.
 */
export interface LegacyChart<Data, Style> extends meistercharts.MeisterChartsApi {
  setData(data: Partial<Data>): void;
  setStyle(style: Partial<Style>): void;
}

/**
 * Data and style as one object, so that `ChartWrapper` can treat them as a single configuration.
 */
export interface LegacyChartConfiguration<Data, Style> {
  data?: Data;
  style?: Style;
}

interface LegacyChartWrapperProps<Data, Style> {
  createChart: () => LegacyChart<Data, Style>;
  configuration?: LegacyChartConfiguration<Data, Style> | undefined;
  style?: React.CSSProperties | undefined;
  className?: string | undefined;
}

/**
 * Adapts a chart of the older data/style API to `ChartWrapper`, which drives every chart through a
 * single `setConfiguration`. The whole React lifecycle stays in `ChartWrapper`.
 */
export function LegacyChartWrapper<Data, Style>({
  createChart: createLegacyChart,
  configuration,
  style,
  className,
}: LegacyChartWrapperProps<Data, Style>) {
  const createChart: () => Chart<LegacyChartConfiguration<Data, Style>> = () => {
    const legacyChart = createLegacyChart();
    return {
      ...legacyChart,
      holder: legacyChart.holder,
      dispose: () => {
        legacyChart.dispose();
      },
      setConfiguration: (config: LegacyChartConfiguration<Data, Style>) => {
        legacyChart.setData(config.data ?? {});
        legacyChart.setStyle(config.style ?? {});
      },
    };
  };

  return <ChartWrapper className={className} configuration={configuration} createChart={createChart} style={style} />;
}
