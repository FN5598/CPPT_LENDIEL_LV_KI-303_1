# Лабораторна робота № 7

**Тема:** Об'єктно-орієнтована обробка даних у Python.  
**Варіант 11:** Аптека.  
**Версія:** `7.0.0`, Python 3.11 або новіший.

## Реалізація

Окремий пакет `lab07` продовжує роботу № 6: CSV-поля й текст звіту збережено.
`Medicine` перевіряє ціну та термін через властивості; `PrescriptionMedicine`
і `FreeMedicine` реалізують `availability_score()`. Аналіз використовує
`AvailableMedicine`, а результати зберігаються у незмінному `ReportSummary`.

| Файл | Призначення |
| --- | --- |
| `models.py` | Базовий клас, властивості, підтипи, Protocol, frozen dataclass |
| `loader.py` | CSV → підтипи, контекст помилок і номер рядка |
| `analysis.py` | Відбір, агрегування, поліморфний вибір |
| `reporting.py` | Детермінований UTF-8 звіт |
| `errors.py` | DataError |
| `cli.py`, `__main__.py` | CLI та коди завершення |
| `tests/` | Поведінкові, інтеграційні й регресійні тести |

## Встановлення, збірка і запуск

З кореня репозиторію, macOS/Linux:

```bash
cd src/main/python/lab07
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e ".[dev]"
python -m lab07 --help
python -m lab07 --version
python -m lab07 --input data/medicines.csv --output reports/summary.txt
python -m pytest
ruff check .
ruff format --check .
python -m build --wheel
```

Windows PowerShell: створіть середовище командою `py -m venv .venv`,
активуйте `.\.venv\Scripts\Activate.ps1`; решта команд однакова.
Після встановлення можна також використовувати команду `lab07`.
Runtime-залежностей немає; `.[dev]` додає pytest, Ruff і build.
Вихідна папка `reports/` і результати збирання ігноруються Git.

## CLI

| Аргумент | Поведінка |
| --- | --- |
| `--help` | Довідка, код 0, без обов'язкових шляхів |
| `--version` | Версія пакета 7.0.0, код 0 |
| `--input CSV` | Вхідний UTF-8 CSV |
| `--output REPORT` | Вихідний текстовий звіт; папки створюються |
| `--max-days-left N` | Верхня включна межа терміну, типово 30 |

Також підтримуються два позиційні шляхи. Змішувати їх з іменованими
не можна. Успіх повертає 0; помилки аргументів, CSV або запису — 2.

## Вхідні дані та результат

Заголовок: `name,form,price,days_left,prescription`.
Назва й форма — непорожні рядки, ціна — скінченне число ≥ 0,
термін — ціле число ≥ 0, рецепт — `true/false`, `yes/no`, `1/0`.
Наприклад: `"Vitamin C, effervescent",Pills,0,0,false`.
Порожній файл або лише заголовок означає порожню колекцію.

`data/medicines.csv` — незмінні 10 записів роботи № 6.
`data/boundaries.csv` — 6 записів обох підтипів, дубль, кома в назві,
нульова ціна та терміни 0, 30 і 31.
`data/empty.csv` і `data/invalid.csv` перевіряють крайові та помилкові сценарії.
Контрольний звіт збережено в `data/expected-report.txt`.

```text
Pharmacy Register Report
CSV records: 10
Selection threshold: up to 30 days remaining, inclusive
Selected records: 9
Medicine count by form:
  Liquid: 6
  Pills: 3
Unique forms: Liquid, Pills
Nearest expiry: Pakistani (Pills), 1 day remaining
Average price of selected medicines: 4.24
Total days remaining: 82.00
Total price: 38.20
```

## Встановлення wheel у чисте середовище

Розпакуйте завантажений ZIP-артефакт і відкрийте папку з wheel:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install ./kzp_lab07_v11-7.0.0-py3-none-any.whl
python -m lab07 --help
python -m lab07 --version
python -c 'from importlib.resources import files; from pathlib import Path; Path("medicines.csv").write_bytes(files("lab07.data").joinpath("medicines.csv").read_bytes())'
python -m lab07 --input medicines.csv --output reports/summary.txt
```

Wheel містить код, CSV і контрольний звіт. Він встановлюється через pip;
після встановлення запуск виконується через `python -m lab07` або `lab07`.
Докладний опис моделі та перевірок — у [REPORT.md](REPORT.md).
