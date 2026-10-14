#!/usr/bin/env sh
set -eu

GRADLE_VERSION="9.6.1"
SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
DIST_ROOT="$SCRIPT_DIR/.gradle-dist"
GRADLE_BIN="$DIST_ROOT/gradle-$GRADLE_VERSION/bin/gradle"
ZIP="$DIST_ROOT/gradle-$GRADLE_VERSION-bin.zip"

if [ ! -x "$GRADLE_BIN" ]; then
  echo "Gradle $GRADLE_VERSION is not bundled. Downloading official distribution once..."
  mkdir -p "$DIST_ROOT"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ZIP"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ZIP" "$URL"
  else
    echo "Need curl or wget to bootstrap Gradle." >&2
    exit 1
  fi
  unzip -q -o "$ZIP" -d "$DIST_ROOT"
  rm -f "$ZIP"
fi

exec "$GRADLE_BIN" "$@"
