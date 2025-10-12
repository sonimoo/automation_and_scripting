# Базовый образ Ubuntu 22.04
FROM ubuntu:22.04

# Установка Python 3, pip и cron, а также библиотеки requests
RUN apt-get update && \
    apt-get install -y python3 python3-pip cron && \
    pip3 install requests

# Установка рабочей директории внутри контейнера
WORKDIR /app

# Копируем скрипт Python в контейнер
COPY currency_exchange_rate.py /app/
# Копируем cronjob в системную папку cron (/etc/cron.d/)
COPY cronjob /etc/cron.d/lab03_cron
# Копируем скрипт entrypoint, который будет запускать cron
COPY entrypoint.sh /app/

# Делаем entrypoint исполняемым
RUN chmod +x /app/entrypoint.sh
# Даем права на cronjob (чтение для cron)
RUN chmod 644 /etc/cron.d/lab03_cron

# Устанавливаем entrypoint: при старте контейнера запускается этот скрипт
ENTRYPOINT ["/app/entrypoint.sh"]
