#!/bin/bash

# Copy the original tesseract-ocr files to the volume directory without overwriting existing files
echo "Copying original files without overwriting existing files"
mkdir -p /usr/share/tessdata
cp -rn /usr/share/tessdata-original/* /usr/share/tessdata

/scripts/init-without-ocr.sh "$@"
