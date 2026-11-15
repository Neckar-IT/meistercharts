#!/bin/sh
#
# Uploads the built examples to the web server serving them.
#
#   MEISTERCHARTS_EXAMPLES_TARGET   scp destination, e.g. user@host:/var/www/<site>/examples
#
# The destination is passed in rather than written here: this module is published, and a
# deployment target names a host, a login and a path of our own infrastructure.

set -e
set -x

[ -n "$MEISTERCHARTS_EXAMPLES_TARGET" ] || {
  echo "MEISTERCHARTS_EXAMPLES_TARGET is not set — expected an scp destination" >&2
  exit 2
}

scp -B -r *.html js "$MEISTERCHARTS_EXAMPLES_TARGET"
