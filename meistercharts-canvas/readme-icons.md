# Icons

## Icon sets / Themes
It is possible to change icons to a different set.

### Default Icon Set
The default icon set provided by Meistercharts uses the material design icons from

https://material.io/resources/icons/?icon=home&style=baseline

of theme *Sharp*. Those icons are black by default. You have to edit them manually to color `#666`.


## Location
The icons are placed as SVG in `src/icons/material`.

## Conversion
The gradle task `createIconDeclarations` converts the image and creates the necessary classes/objects.

### Providers

These providers should *not* be used directly. Instead use the image ids defined in `Icons` to resolve
the images using the `ImageRegistry`.

#### SVG-based Icons
The object `com.cedarsoft.resources.svg.SvgPaintables` contains paintables that are backed by icons.
