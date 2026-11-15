import { createBarChartGrouped } from "@meistercharts/meistercharts";
import { readFileSync, realpathSync } from "node:fs";
import { resolve } from "node:path";
import { describe, expect, it } from "vitest";

const linkedPackageDirectory: string = realpathSync(resolve(import.meta.dirname, "../node_modules/@meistercharts/meistercharts"));

describe("the linked meistercharts-easy-api bundle", () => {
  it("resolves to the build output of the current source", () => {
    expect(linkedPackageDirectory.endsWith("meistercharts-easy-api/build/npm-work")).toBe(true);
  });

  it("has the name and the version of the npm bundle", () => {
    const packageJson: unknown = JSON.parse(readFileSync(resolve(linkedPackageDirectory, "package.json"), "utf-8"));
    const expectedVersion: string = readFileSync(resolve(import.meta.dirname, "../../../../../meistercharts.version"), "utf-8").trim();

    expect(packageJson).toMatchObject({ name: "@meistercharts/meistercharts", version: expectedVersion });
  });

  it("exports the chart API", () => {
    expect(createBarChartGrouped).toBeTypeOf("function");
  });
});
