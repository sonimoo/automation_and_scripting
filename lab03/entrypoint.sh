#!/bin/bash
# EntryPoint скрипт для запуска cron в контейнере и логирования его работы

# Функция для создания лог-файла и назначения прав на запись
create_log_file() {
    echo "Creating log file..."
    touch /var/log/cron.log       # создаём файл лога
    chmod 666 /var/log/cron.log   # даём права на запись для всех пользователей
    echo "Log file created at /var/log/cron.log"
}

# Функция для мониторинга лог-файла в реальном времени
monitor_logs() {
    echo "=== Monitoring cron logs ==="
    tail -f /var/log/cron.log     # выводим новые строки лога по мере их появления
}

# Функция для запуска cron-демона в foreground
run_cron() {
    echo "=== Starting cron daemon ==="
    exec cron -f                  # запускаем cron в foreground, чтобы контейнер не завершился
}

# Сохраняем текущие переменные окружения в системный файл
env > /etc/environment

# Создаём лог-файл
create_log_file

# Запускаем мониторинг лога в фоне
monitor_logs &

# Запускаем cron
run_cron
