# Charting Algorithms 

This module contains algorithm implementations. 
All content within this module is independent from any 
UI framework.

Ideally the code can be ported to JavaScript / TypeScript without
any problems.


# Basic Ideas

## Layers

There are five layers with different values.
Each layer is calculated from the previous layer using
one single calculation.


### Domain in domain specific values
#### @Domain

Contains the data points with their specific domain values (e.g. Volt) 


### Domain (relative) in percent
#### @DomainRelative

Contains the domain values as percentage within their value range.

### ContentArea (relative) in percent
#### @ContentAreaRelative

Values relative to the height of the content area (not the window)
Axis orientation is calculated into this value.
The content area relative values are always relative to the origin of
the screen (top left).

### ContentArea in pixels
#### @ContentArea

Contains the values as they occur in the content area size.


### Zoomed in pixels
#### @Zoomed

Contains the domain values as zoomed pixels. This value is
especially relevant when calculating with lengths.


### Window (zoomed and translated) in pixels
#### @Window
 
The values as pixels relative to the window. 
 











