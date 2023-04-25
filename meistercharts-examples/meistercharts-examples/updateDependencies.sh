#!/bin/sh

if [ -z "$1" ]; then
  EXAMPLE_PATH="./"
else
  EXAMPLE_PATH=$1
fi

# This script checks all example projects for newer
# dependencies with yarn + npm. it also installs major updates

PROJECTS=$(find "$EXAMPLE_PATH" -maxdepth 1 -type d)

# https://www.npmjs.com/package/npm-check-updates
if ! which ncu > /dev/null; then
  echo "npm-check-updates is not installed, installing..."
  npm install -g npm-check-updates@latest
fi

# iterate over all example projects
for PROJECT in $PROJECTS; do
  # check if it is a directory and if it contains a package.json
  if [ -d "$PROJECT" ] && [ -f "$PROJECT/package.json" ]; then
    cd "$PROJECT"
    # check yarn or npm package manager
    if [ -f "yarn.lock" ]; then
      # update yarn dependencies
      yarn upgrade --latest
    elif [ -f "package-lock.json" ]; then
      # update npm dependencies
      ncu -u
      npm update --save-dev
    else
      echo "Error: No Dependencies Found in project <$PROJECT>"
    fi
    # return to origin directory
    cd -
    echo "Updated dependencies of <$PROJECT>"
  fi
done
