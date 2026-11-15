/**
 * Custom Error for when the ref to the chart is not initialized yet
 */
export class ChartNotInitializedError extends Error {
  constructor() {
    super("The chart was used before it was initialized.");
    this.name = "ChartNotInitializedError";
  }
}

/**
 * Custom Error for when the ref to the chart container div is not initialized yet
 */
export class ContainerNotInitializedError extends Error {
  constructor() {
    super("The container for the chart was used before it was initialized.");
    this.name = "ContainerNotInitializedError";
  }
}
