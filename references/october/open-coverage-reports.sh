#!/bin/bash

# This script finds all JaCoCo reports in the project and opens them in the default web browser.
# It's written to handle file paths with spaces or other special characters correctly.

find . -path '*/target/site/jacoco/index.html' -print0 | while IFS= read -r -d $'\0' report; do
  # Use xdg-open to open the report in the default browser on Linux.
  # For macOS, you might want to use 'open' instead.
  xdg-open "$report"
done
