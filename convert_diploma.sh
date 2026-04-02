#!/bin/bash
# Конвертация diploma.md → diploma.docx с оформлением по ГОСТ/методичке КФУ
# Требования: pandoc >= 2.14, шрифт Times New Roman установлен в системе
#
# Установка pandoc:
#   macOS:   brew install pandoc
#   Ubuntu:  sudo apt install pandoc
#   Windows: https://pandoc.org/installing.html

set -e

INPUT="diploma.md"
OUTPUT="diploma.docx"
REFERENCE="diploma_reference.docx"  # если есть — используется как шаблон

# Основные параметры ГОСТ / методичка КФУ:
#   Шрифт:        Times New Roman 14pt
#   Поля:         левое 30мм, правое 10мм, верхнее 20мм, нижнее 25мм
#   Межстрочный:  1.5
#   Отступ абзаца: 1.25 см
#   Выравнивание:  по ширине

PANDOC_VARS=(
  --variable mainfont="Times New Roman"
  --variable fontsize=14pt
  --variable geometry="left=30mm,right=10mm,top=20mm,bottom=25mm"
  --variable linestretch=1.5
  --variable indent=true
)

# Если есть reference-файл (шаблон .docx с настройками стилей) — используем его
if [ -f "$REFERENCE" ]; then
  echo "Используем reference-документ: $REFERENCE"
  REFERENCE_OPT="--reference-doc=$REFERENCE"
else
  REFERENCE_OPT=""
fi

echo "Конвертирую $INPUT → $OUTPUT ..."

pandoc "$INPUT" \
  -o "$OUTPUT" \
  --from markdown \
  --to docx \
  $REFERENCE_OPT \
  "${PANDOC_VARS[@]}" \
  --toc \
  --toc-depth=3 \
  --number-sections \
  --standalone

echo "Готово: $OUTPUT"
echo ""
echo "Совет: откройте $OUTPUT в Word/LibreOffice и проверьте:"
echo "  - Шрифт заголовков (должен быть TNR 14pt жирный)"
echo "  - Поля (Файл → Параметры страницы)"
echo "  - Нумерацию страниц (вставьте вручную если нет)"
