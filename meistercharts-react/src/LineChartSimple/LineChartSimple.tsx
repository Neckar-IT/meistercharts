import * as meistercharts from "@meistercharts/meistercharts";
import { LegacyChartWrapper } from "../utils/LegacyChartWrapper";

export interface LineChartSimpleConfiguration {
  data: meistercharts.LineChartSimpleData;
  style?: meistercharts.LineChartSimpleStyle;
}

export interface LineChartSimpleProps {
  configuration: LineChartSimpleConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Line Chart Simple Component
 */
export function LineChartSimple({ className, configuration, style }: LineChartSimpleProps) {
  return (
    <LegacyChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createLineChartSimple}
      style={style}
    />
  );
}
