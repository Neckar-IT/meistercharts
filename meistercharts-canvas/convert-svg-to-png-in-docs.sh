#!/bin/bash

function convertSvgToPng {
  dir=$1

  for filename in $dir/*.svg; do
    base=`basename $filename .svg`

    inkscape -z -D -e $dir/$base.png $filename
  done
}


convertSvgToPng "src/main/doc"
convertSvgToPng "src/main/doc/translation"
convertSvgToPng "src/main/doc/tiling"
