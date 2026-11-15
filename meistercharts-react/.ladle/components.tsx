import { type GlobalProvider, ThemeState } from "@ladle/react";
import * as meistercharts from "@meistercharts/meistercharts";
import { StrictMode, useEffect } from "react";

export const Provider: GlobalProvider = ({ children, globalState }) => {
  useEffect(() => {
    switch (globalState.theme) {
      case ThemeState.Light:
        meistercharts.setDefaultTheme("Default");
        break;
      case ThemeState.Dark:
        meistercharts.setDefaultTheme("Dark");
        break;
      case ThemeState.Auto:
        meistercharts.setDefaultTheme("Default");
        break;
      default:
        throw new Error(`Unexpected value in Switch Statement: globalState.theme`);
    }
  }, [globalState.theme]);

  return <StrictMode>{children}</StrictMode>;
};
