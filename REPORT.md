# Звіти до лабораторних робіт

Цей файл містить перелік лабораторних робіт із дисципліни
«Кросплатформенні засоби програмування» та посилання на їхню документацію.
Спільна предметна область — лікарські засоби, варіант 11.

## Лабораторні роботи

| Робота | Мова | Тема | Документація | Звіт |
| --- | --- | --- | --- | --- |
| 1 | Java | Читання, перевірка й аналіз CSV | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab01/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab01/REPORT.md) |
| 2 | Java | Структуровані моделі й підсумкові значення | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab02/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab02/REPORT.md) |
| 3 | Java | Наслідування, поліморфізм і рівність об'єктів | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab03/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab03/REPORT.md) |
| 4 | Java | Обробка колекцій через Stream API | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab04/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab04/REPORT.md) |
| 5 | Java | Generic repository та CSV persistence через reflection | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab05/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab05/REPORT.md) |
| 6 | Python | Модульна обробка CSV, колекції, CLI та wheel | [README](src/main/python/lab06/README.md) | [REPORT](src/main/python/lab06/REPORT.md) |

## Структура робіт

Роботи № 1–5 розвивають Java-модель і використовують Maven, JUnit та SpotBugs.
Робота № 6 є окремим Python-пакетом у `src/main/python/lab06/` із власним CSV,
pytest-тестами й конфігурацією Ruff. Її результат —
текстовий звіт `src/main/python/lab06/reports/summary.txt` для ліків
із терміном придатності до 30 днів включно.

Команди встановлення, тестування й запуску наведено в кореневому
[README.md](README.md) та в README відповідної роботи. Деталі реалізації,
результати перевірок, висновки й використання ШІ описано в окремих звітах.
