#!/usr/bin/env bash

#
# This script converts all svg files to matching PNG files (with transparent background)
#

set -e
set -x

cd src/commonMain/resources/elektromeister || exit 7

pwd

# Find all SVG files and convert them to PNG
for svg_file in *.svg; do
  echo "Converting $svg_file to PNG"
  png_file="${svg_file%.svg}.png"
  convert -background none "$svg_file" "$png_file"
done
