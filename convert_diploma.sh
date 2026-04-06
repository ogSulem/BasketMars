#!/bin/bash
# Конвертация diploma.md → diploma.docx с оформлением по ГОСТ/методичке КФУ
#
# ОБЯЗАТЕЛЬНЫЕ ТРЕБОВАНИЯ:
#   1. pandoc >= 2.14
#        macOS:   brew install pandoc
#        Ubuntu:  sudo apt install pandoc
#        Windows: https://pandoc.org/installing.html
#
#   2. diploma_reference.docx — шаблон Word с ГОСТ-стилями
#        Если файла нет — сначала запустите: python3 create_reference.py
#        (требует: pip install python-docx)
#
# ВАЖНО: параметры --variable fontsize/geometry/linestretch работают ТОЛЬКО
# для PDF-вывода (через LaTeX). Для DOCX форматирование задаётся ИСКЛЮЧИТЕЛЬНО
# через --reference-doc. Без diploma_reference.docx шрифт и поля будут
# произвольными — не соответствующими методическим указаниям.
#
# СКРИНШОТЫ: замените файлы в папке screenshots/ на реальные скриншоты.
# Папка и placeholder-имена описаны в разделе 3.7 diploma.md.

set -e

INPUT="diploma.md"
OUTPUT="diploma.docx"
REFERENCE="diploma_reference.docx"

# Проверяем наличие pandoc
if ! command -v pandoc &> /dev/null; then
  echo "ОШИБКА: pandoc не найден."
  echo "  macOS:   brew install pandoc"
  echo "  Ubuntu:  sudo apt install pandoc"
  echo "  Windows: https://pandoc.org/installing.html"
  exit 1
fi

# Создаём папку screenshots если не существует (pandoc не упадёт на отсутствующих изображениях)
mkdir -p screenshots

# Проверяем наличие reference-документа
if [ ! -f "$REFERENCE" ]; then
  echo "ПРЕДУПРЕЖДЕНИЕ: файл $REFERENCE не найден."
  echo "  Для корректного ГОСТ-форматирования запустите сначала:"
  echo "    python3 create_reference.py"
  echo ""
  echo "  Продолжаю конвертацию БЕЗ шаблона (форматирование будет стандартным pandoc)..."
  REFERENCE_OPT=""
else
  echo "Используем reference-документ: $REFERENCE"
  REFERENCE_OPT="--reference-doc=$REFERENCE"
fi

echo "Конвертирую $INPUT → $OUTPUT ..."

pandoc "$INPUT" \
  -o "$OUTPUT" \
  --from markdown \
  --to docx \
  $REFERENCE_OPT \
  --toc \
  --toc-depth=3 \
  --standalone

echo ""
echo "Готово: $OUTPUT"
echo ""
echo "Следующие шаги (обязательно проверить в Word/LibreOffice):"
echo "  1. Шрифт основного текста: Times New Roman 14pt"
echo "  2. Поля: левое 30мм, правое 15мм, верхнее 20мм, нижнее 20мм"
echo "     (Макет → Поля → Настраиваемые поля)"
echo "  3. Межстрочный интервал: 1.5 (Главная → Интервал)"
echo "  4. Абзацный отступ: 1.25 см (Главная → Абзац → Отступ)"
echo "  5. Нумерация страниц: Вставка → Номер страницы (с 3-й страницы)"
echo "  6. Заменить placeholder-изображения в разделе 3.7 на реальные скриншоты"
echo "  7. Обновить автоматическое оглавление (ПКМ → Обновить поле)"
