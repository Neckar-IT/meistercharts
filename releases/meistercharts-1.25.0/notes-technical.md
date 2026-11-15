### Changed

- renamed interface `DataSeries` to `DecimalDataSeries`
- `Sample`: renamed property `values` to `decimalValues`
- `TimeLineChart`: changed signature of `setDataSeries` to `setDataSeries(jsDecimalDataSeries: Array<DecimalDataSeries>,jsEnumDataSeries: Array<EnumDataSeries>)`
- `TimeLineChartStyle`: changed property `dataSeriesConfigurations` to `decimalDataSeriesStyles` of type `Array<DecimalDataSeriesStyle>?`
- renamed `DataSeriesConfiguration` to `DecimalDataSeriesStyle` and renamed its property `label` to `valueAxisTitle`
