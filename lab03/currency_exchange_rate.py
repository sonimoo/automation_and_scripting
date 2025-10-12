#!/usr/bin/env python3

import argparse
import os
import sys
import json
import logging
from datetime import datetime
import requests

# URL сервиса
SERVICE_URL = "http://localhost:8080/"

# список валют (для предупреждений)
ALLOWED_CURRENCIES = {"MDL", "USD", "EUR", "RON", "RUS", "UAH", "GBP", "CHF", "JPY"}

# Допустимый период данных
MIN_DATE = datetime.strptime("2025-01-01", "%Y-%m-%d").date()
MAX_DATE = datetime.strptime("2025-09-15", "%Y-%m-%d").date()


def setup_logging(project_root):
    log_path = os.path.join(project_root, "error.log")
    logger = logging.getLogger("currency_exchange_rate")
    logger.setLevel(logging.INFO)
    if logger.handlers:
        return logger

    ch = logging.StreamHandler(sys.stdout)
    ch.setLevel(logging.INFO)
    ch.setFormatter(logging.Formatter("%(levelname)s: %(message)s"))
    logger.addHandler(ch)

    fh = logging.FileHandler(log_path, encoding="utf-8")
    fh.setLevel(logging.ERROR)
    fh.setFormatter(logging.Formatter("%(asctime)s %(levelname)s: %(message)s"))
    logger.addHandler(fh)

    return logger


def validate_date(date_str):
    try:
        d = datetime.strptime(date_str, "%Y-%m-%d").date()
    except ValueError:
        raise ValueError("Дата должна быть в формате YYYY-MM-DD")
    if not (MIN_DATE <= d <= MAX_DATE):
        raise ValueError(f"Дата должна быть в интервале {MIN_DATE} - {MAX_DATE}")
    return d


def save_json(project_root, from_cur, to_cur, date_str, payload):
    data_dir = os.path.join(project_root, "data")
    os.makedirs(data_dir, exist_ok=True)
    filename = f"{from_cur.upper()}_to_{to_cur.upper()}_{date_str}.json"
    path = os.path.join(data_dir, filename)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, ensure_ascii=False, indent=2)
    return path


def call_service(from_cur, to_cur, date_str, api_key, timeout=8):
    """
    GET: from, to, date
    POST: key
    """
    params = {"from": from_cur, "to": to_cur, "date": date_str}
    data = {"key": api_key}
    resp = requests.post(SERVICE_URL, params=params, data=data, timeout=timeout)
    resp.raise_for_status()
    return resp.json()


def main():
    parser = argparse.ArgumentParser(description="Get currency exchange rate from local service")
    parser.add_argument("from_currency")
    parser.add_argument("to_currency")
    parser.add_argument("date")
    parser.add_argument("--key", help="API key (overrides env)", default="1234")
    args = parser.parse_args()

    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.abspath(os.path.join(script_dir, ".."))
    logger = setup_logging(project_root)

    from_cur = args.from_currency.strip().upper()
    to_cur = args.to_currency.strip().upper()

    if from_cur not in ALLOWED_CURRENCIES:
        logger.info(f"Warning: {from_cur} not in predefined list. Requesting anyway.")
    if to_cur not in ALLOWED_CURRENCIES:
        logger.info(f"Warning: {to_cur} not in predefined list. Requesting anyway.")

    try:
        date_obj = validate_date(args.date)
        date_str = date_obj.isoformat()
    except ValueError as e:
        logger.error(str(e))
        print(f"Ошибка: {e}")
        sys.exit(1)

    api_key = args.key

    try:
        logger.info(f"Запрос курса {from_cur} -> {to_cur} на {date_str} ...")
        resp_json = call_service(from_cur, to_cur, date_str, api_key)
    except requests.exceptions.RequestException as e:
        logger.error(f"Network/HTTP error while calling service: {e}")
        print(f"Ошибка запроса: {e}")
        sys.exit(2)

    if isinstance(resp_json, dict) and resp_json.get("error"):
        err_msg = resp_json.get("error")
        logger.error(f"Service returned error: {err_msg}")
        print(f"Ошибка сервиса: {err_msg}")
        sys.exit(3)

    try:
        path = save_json(project_root, from_cur, to_cur, date_str, resp_json)
        logger.info(f"Данные сохранены в {path}")
        print(f"Успех: данные сохранены в {path}")
    except Exception as e:
        logger.error(f"Failed to save JSON: {e}")
        print(f"Ошибка при сохранении файла: {e}")
        sys.exit(4)


if __name__ == "__main__":
    main()
