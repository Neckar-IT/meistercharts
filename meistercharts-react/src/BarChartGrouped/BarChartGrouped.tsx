import * as meistercharts from "@meistercharts/meistercharts";
import { LegacyChartWrapper } from "../utils/LegacyChartWrapper";

export interface BarChartGroupedConfiguration {
  data: meistercharts.BarChartGroupedData;
  style?: meistercharts.BarChartGroupedStyle;
}

export interface BarChartGroupedProps {
  configuration: BarChartGroupedConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Bar Chart Grouped Component
 */
export function BarChartGrouped({ className, configuration, style }: BarChartGroupedProps) {
  return (
    <LegacyChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createBarChartGrouped}
      style={style}
    />
  );
}
