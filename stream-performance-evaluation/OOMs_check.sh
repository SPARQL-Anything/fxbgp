#!/bin/bash

# Uso: ./search_oom.sh [directory] [pattern]
DIR="${1:-.}"
PATTERN="${2:-Exception|OutOfMemory}"

if [ ! -d "$DIR" ]; then
  echo "Errore: '$DIR' non è una directory valida."
  exit 1
fi

echo "Cerco '$PATTERN' nei file di testo in: $DIR"
echo "---"

found=0
while IFS= read -r -d '' file; do
  if grep -qE "$PATTERN" "$file" 2>/dev/null; then
    echo "TROVATO: $file"
    grep -nE "$PATTERN" "$file" | while IFS= read -r line; do
      echo "  $line"
    done
    found=1
  fi
done < <(find "$DIR" -type f \( -name "*.tsv" -o -name "*.log" \) -print0)

echo "---"
if [ "$found" -eq 0 ]; then
  echo "Nessun file contiene '$PATTERN'."
  exit 1
else
  exit 0
fi