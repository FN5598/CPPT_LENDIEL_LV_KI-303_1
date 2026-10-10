# CPPT Laboratory Works

Лабораторні роботи з дисципліни «Кросплатформенні засоби програмування»,
варіант 11 — лікарські засоби. Роботи № 1–5 реалізовано на Java,
роботу № 6 — як окремий Python-пакет.

## Структура проєкту

```text
src/main/java/                      Java-код і документація робіт № 1–5
src/main/resources/                 CSV та інші ресурси Java-програм
src/test/java/                      Модульні тести Java
src/main/java/.../lab01/             Код і документація лабораторної роботи № 1
src/main/java/.../lab02/             Код і документація лабораторної роботи № 2
src/main/java/.../lab03/             Код і документація лабораторної роботи № 3
src/main/java/.../lab04/             Код і документація лабораторної роботи № 4
src/main/java/.../lab05/             Код і документація лабораторної роботи № 5
src/main/python/lab06/               Python-код і документація роботи № 6
src/main/python/lab06/tests/         Модульні тести Python
src/main/python/lab06/data/          Вхідні CSV роботи № 6
src/main/python/lab06/reports/       Приклад текстового звіту роботи № 6
REPORT.md                           Загальний перелік звітів
pom.xml                             Конфігурація Maven для Java
src/main/python/lab06/pyproject.toml Конфігурація Python-пакета
```

## Лабораторні роботи

| Робота | Опис                               | Документація              | Звіт                      |
| ------ | ---------------------------------- | ------------------------- | ------------------------- |
| 1      | Обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab01/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab01/REPORT.md) |
| 2      | Структурована обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab02/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab02/REPORT.md) |
| 3      | Наслідування та поліморфна модель лікарських засобів | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab03/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab03/REPORT.md) |
| 4      | Обробка колекції через Stream API | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab04/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab04/REPORT.md) |
| 5      | Generic repository і CSV persistence через reflection | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab05/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab05/REPORT.md) |
| 6      | Обробка аптечних CSV-даних засобами Python | [README](src/main/python/lab06/README.md) | [REPORT](src/main/python/lab06/REPORT.md) |

## Вимоги

- Роботи № 1–5: Java 21 або новіша та Maven Wrapper із репозиторію.
- Робота № 6: Python 3.11 або новіший із `pip` і `venv`.
- Git для роботи з історією версій.

## Збірка і тестування

### Java — роботи № 1–5

Команди виконуються з кореня репозиторію.

Linux/macOS:

```bash
./mvnw clean test
./mvnw verify
./mvnw package
```

Windows:

```bat
mvnw.cmd clean test
mvnw.cmd verify
mvnw.cmd package
```

`verify` виконує Java-тести, створює JAR і запускає SpotBugs.

### Python — робота № 6

З кореня репозиторію, macOS/Linux:

```bash
cd src/main/python/lab06
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -e ".[dev]"
python -m pytest
ruff check .
ruff format --check .
python -m build
python -m lab06 --input data/medicines.csv --output reports/summary.txt
```

Для Windows PowerShell використовуйте `python -m venv .venv` та
`.\.venv\Scripts\Activate.ps1`; решта команд після активації однакова.
`.[dev]` встановлює pytest, Ruff і build. Maven запускає Java-перевірки,
а Python-перевірки виконуються окремо через pytest і Ruff.
Повний опис CLI та команд — у [README роботи № 6](src/main/python/lab06/README.md).

## Ресурси

Java-ресурси розміщуються в `src/main/resources`, зокрема
[Data.csv](src/main/resources/lab01/Data.csv). Python-робота використовує
[medicines.csv](src/main/python/lab06/data/medicines.csv), а приклад результату
створюється в `src/main/python/lab06/reports/summary.txt`.

## Звіти

Повний перелік звітів доступний у [REPORT.md](REPORT.md).

## Версії та релізи

Java-роботи № 1–5 є послідовними релізами продукту:

| Лабораторна | Версія POM і `--version` | Git tag |
| --- | --- | --- |
| 1 | `1.0.0` | `v1.0.0` |
| 2 | `2.0.0` | `v2.0.0` |
| 3 | `3.0.0` | `v3.0.0` |
| 4 | `4.0.0` | `v4.0.0` |
| 5 | `5.0.0` | `v5.0.0` |

Java POM має версію `5.0.0`; попередні Java-релізи доступні у відповідних
release-гілках і тегах. Версію Java-програм можна перевірити аргументом
`--version`.

Python-робота № 6 має окрему версію пакета `6.0.0`, задану в
[pyproject.toml](src/main/python/lab06/pyproject.toml). Вона встановлюється
й пакується окремо від Maven-проєкту; її CLI підтримує `--help`, `--version`,
`--input`, `--output` і `--max-days-left`. Перевірка версії після встановлення:

```bash
python -m lab06 --version
```
