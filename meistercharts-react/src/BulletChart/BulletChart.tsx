import * as meistercharts from "@meistercharts/meistercharts";
import { ChartWrapper } from "../utils/ChartWrapper";

export interface BulletChartProps {
  configuration: meistercharts.BulletChartConfiguration;
  style?: React.CSSProperties;
  className?: string;
}

/**
 * Bullet Chart Component
 */
export function BulletChart({ className, configuration, style }: BulletChartProps) {
  return (
    <ChartWrapper
      className={className}
      configuration={configuration}
      createChart={meistercharts.createBulletChart}
      style={style}
    />
  );
}
