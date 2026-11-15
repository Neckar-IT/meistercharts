import * as meistercharts from "@meistercharts/meistercharts";
import { LegacyChartWrapper } from "../utils/LegacyChartWrapper";

export interface HistogramConfiguration {
  data: meistercharts.HistogramData;
  style?: meistercharts.HistogramStyle;
}

export interface HistogramConfigurationProps {
  configuration: HistogramConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Histogram Component
 */
export function Histogram({ className, configuration, style }: HistogramConfigurationProps) {
  return (
    <LegacyChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createHistogram}
      style={style}
    />
  );
}
