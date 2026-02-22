#!/usr/bin/env python3
"""Convert holidays-jp API format to PassAlarm app format."""

import json
import glob
import os
import sys

HOLIDAYS_DIR = os.path.join(os.path.dirname(__file__), '..', '..', 'packages', 'holidays')

def convert_api_to_app(api_data: dict, year: int) -> dict:
    holidays = []
    for date_str, name in sorted(api_data.items()):
        holidays.append({"date": date_str, "name": name})
    return {
        "year": year,
        "country": "JP",
        "holidays": holidays
    }

def main():
    api_files = glob.glob(os.path.join(HOLIDAYS_DIR, 'holidays_jp_*_api.json'))
    if not api_files:
        print("No API files found. Skipping conversion.")
        return

    for api_file in sorted(api_files):
        basename = os.path.basename(api_file)
        # Extract year from filename like holidays_jp_2025_api.json
        parts = basename.replace('.json', '').split('_')
        year_str = parts[2]  # holidays_jp_YYYY_api
        try:
            year = int(year_str)
        except ValueError:
            print(f"Skipping {basename}: cannot parse year")
            continue

        with open(api_file, 'r', encoding='utf-8') as f:
            api_data = json.load(f)

        app_data = convert_api_to_app(api_data, year)
        output_file = os.path.join(HOLIDAYS_DIR, f'holidays_jp_{year}.json')

        with open(output_file, 'w', encoding='utf-8') as f:
            json.dump(app_data, f, ensure_ascii=False, indent=2)

        print(f"Converted {basename} -> holidays_jp_{year}.json ({len(app_data['holidays'])} holidays)")

if __name__ == '__main__':
    main()
