import { vi } from "vitest";

// The bundle creates a canvas context while it loads, and jsdom implements no canvas.
class CanvasRenderingContext2DStub {}

const contextStub = new Proxy(new CanvasRenderingContext2DStub(), {
  get: (target: object, property: string | symbol): unknown => (property in target ? Reflect.get(target, property) : (): void => {}),
});

vi.stubGlobal("CanvasRenderingContext2D", CanvasRenderingContext2DStub);
vi.spyOn(HTMLCanvasElement.prototype, "getContext").mockReturnValue(contextStub as CanvasRenderingContext2D);
