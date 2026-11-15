import * as meistercharts from "@meistercharts/meistercharts";
import { LegacyChartWrapper } from "../utils/LegacyChartWrapper";

export interface BarChartStackedConfiguration {
  data: meistercharts.BarChartData;
  style?: meistercharts.BarChartStackedStyle;
}

export interface BarChartStackedProps {
  configuration: BarChartStackedConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Bar Chart Stacked Component
 */
export function BarChartStacked({ className, configuration, style }: BarChartStackedProps) {
  return (
    <LegacyChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createBarChartStacked}
      style={style}
    />
  );
}
