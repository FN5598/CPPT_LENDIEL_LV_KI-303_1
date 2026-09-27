# Лабораторна робота № 2

## Статус

Лабораторна робота містить Java-програму для структурованого читання, перевірки та узагальнення даних про лікарські засоби.

## Код і ресурси

- Код: [`Lab02Application.java`](Lab02Application.java)
- Вхідні дані: [`Data.csv`](../../../../../../resources/lab01/Data.csv)
- Звіт: [`REPORT.md`](REPORT.md)

Програма перетворює кожен коректний CSV-рядок на об'єкт `MedicineInformation`, перевіряє його поля та обчислює середню ціну, кількість рецептурних препаратів, найкоротший термін придатності й загальну кількість коректних рядків. Результати зберігаються у записі `MedicineInformationSummary`.

## Збірка і тестування

```bash
./mvnw clean test
./mvnw package
java -cp target/classes ua.lpnu.lendiel.vitalii.lab02.Lab02Application
```

## Наступні кроки

- додати окремі модульні тести для `MedicineInformation.fromCsv(...)`;
- перевірити обробку порожніх і некоректних CSV-полів;
- доповнити звіт результатами запуску та посиланнями на GitHub Issues/Pull Request.

## Executable release JAR

```bash
./mvnw package
java -jar target/CPPT_LAB_WORKS-2.0.0.jar --help
java -jar target/CPPT_LAB_WORKS-2.0.0.jar --input data/input.csv --output target/lab02/report.txt
```

`--input` and `--output` can be used independently. Without `--input`,
the application reads the bundled `Data.csv`. The input is the five-column semicolon-delimited medicine CSV. The output is the UTF-8 text report.
The release branch CI runs on branch pushes and uploads a JAR for each OS.
