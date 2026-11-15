import * as meistercharts from "@meistercharts/meistercharts";
import { LegacyChartWrapper } from "../utils/LegacyChartWrapper";

export interface CompassConfiguration {
  data: meistercharts.CompassData;
  style?: meistercharts.CompassStyle;
}

export interface CompassProps {
  configuration: CompassConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Compass Component
 */
export function Compass({ className, configuration, style }: CompassProps) {
  return (
    <LegacyChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createCompass}
      style={style}
    />
  );
}
