#!/bin/bash

# Проверка, что указан хотя бы один аргумент
if [ $# -lt 1 ]; then
    echo "Ошибка: нужно указать путь к директории."
    echo "Использование: $0 <директория> [расширения...]"
    exit 1
fi

DIR=$1
shift   # убираем первый аргумент (директория), остальные — расширения

# Проверяем, что директория существует
if [ ! -d "$DIR" ]; then
    echo "Ошибка: директория '$DIR' не существует."
    exit 1
fi

# Если расширения не указаны, используем .tmp по умолчанию
if [ $# -eq 0 ]; then
    EXTENSIONS=("tmp")
else
    EXTENSIONS=("$@")
fi

# Счётчик удалённых файлов
count=0

# Удаляем файлы с указанными расширениями
for ext in "${EXTENSIONS[@]}"; do
    files=$(find "$DIR" -type f -name "*.$ext")
    for file in $files; do
        rm -f "$file"
        ((count++))
    done
done

echo "Удалено файлов: $count"