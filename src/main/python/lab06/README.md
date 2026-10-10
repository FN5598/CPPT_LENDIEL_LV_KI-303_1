# Лабораторна робота № 6

## Статус

Лабораторна робота містить Python-пакет для читання, перевірки та аналізу
аптечного CSV варіанта 11. Відбираються ліки з `days_left <= 30`, обчислюються
кількість за формою, унікальні форми, найближчий термін і середня ціна.
Для запуску потрібний Python 3.11 або новіший.

## Код і ресурси

- Модель запису: [models.py](models.py).
- Завантаження та валідація: [loader.py](loader.py).
- Аналіз: [analysis.py](analysis.py).
- Форматування й запис: [reporting.py](reporting.py).
- Помилки: [errors.py](errors.py).
- Командний рядок: [cli.py](cli.py), [__main__.py](__main__.py).
- Вхідні дані: [medicines.csv](data/medicines.csv).
- Вихідний звіт: `reports/summary.txt` (створюється під час запуску).
- Тести: [tests/](tests/).
- Звіт: [REPORT.md](REPORT.md).

```text
CSV → loader → list[MedicineRecord] → analysis → reporting → UTF-8 звіт
```

## Формат CSV

```csv
name,form,price,days_left,prescription
```

Назва й форма — непорожній текст, ціна — скінченне невід'ємне число,
термін — невід'ємне ціле число, рецепт — `true/false`, `yes/no` або `1/0`.
Поля з комою беруться в лапки; внутрішні лапки подвоюються:

```csv
"Vitamin C, ""Plus""",Pills,12.50,30,yes
```

Нульовий файл і правильний заголовок без записів дають порожній список.
Пошкоджений CSV або некоректне значення викликає `DataError`; перевірка
значення містить номер фізичного рядка й назву поля.

## Збірка і тестування

Команди виконуються з цього каталогу (`src/main/python/lab06/`).

macOS/Linux:

```bash
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e ".[dev]"
python -m pytest
ruff check .
ruff format --check .
python -m build
```

Windows PowerShell:

```powershell
python -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -e ".[dev]"
python -m pytest
ruff check .
ruff format --check .
python -m build
```

`.[dev]` встановлює пакет, pytest, Ruff і build. Для виправлення оформлення
використовується `ruff format .`. Wheel створюється в `dist/`.
Тести перевіряють коректні, граничні й пошкоджені CSV, порожній вхід,
відбір, метрики, фізичні номери помилок, UTF-8-запис і CLI.

## Командний рядок

Після встановлення пакета:

```bash
python -m lab06 --help
python -m lab06 --version
python -m lab06 --input data/medicines.csv --output reports/summary.txt
python -m lab06 --input data/medicines.csv --output reports/custom.txt --max-days-left 10
```

Вхідний і вихідний шляхи задаються парою `--input` і `--output`;
типовий поріг — 30 днів включно. `--help` і `--version` працюють без шляхів.
Попередній виклик `python -m lab06 data/medicines.csv reports/summary.txt`
також підтримується. Позиційні шляхи та іменовані прапорці не змішуються.
Успіх повертає `0`, очікувана помилка даних або файла — `2` із повідомленням
у `stderr`. У новому терміналі потрібно знову активувати `.venv`.

## Приклад результату

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

Повний опис реалізації, локальних перевірок і встановлення wheel наведено
в [REPORT.md](REPORT.md).
