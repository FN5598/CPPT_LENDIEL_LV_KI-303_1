# Лабораторна робота № 1

## Статус

Лабораторна робота містить Java-програму для читання та перевірки даних про
лікарські засоби варіанта 11. Формат одного рядка:

```text
name;form;price;expirationDays;prescription
```

Перевіряються рівно п'ять полів, форма `Liquid`/`Pills`, невід'ємна скінченна
ціна, невід'ємний термін і `true`/`false`. Некоректний рядок не впливає на
статистику; помилка містить його фізичний номер.

## Код і ресурси

- Код: [`Lab01Application.java`](Lab01Application.java)
- Вхідні дані: [`Data.csv`](../../../../../../resources/lab01/Data.csv)
- Звіт: [`REPORT.md`](REPORT.md)

Програма обчислює середню ціну, кількість рецептурних препаратів,
найкоротший термін придатності й кількість коректних рядків. Числа форматуються
через `Locale.ROOT`, а звіт однаково друкується в консоль і зберігається в
UTF-8-файл.

## Командний рядок

```bash
java -cp target/classes ua.lpnu.lendiel.vitalii.lab01.Lab01Application --help
java -cp target/classes ua.lpnu.lendiel.vitalii.lab01.Lab01Application \
  --input data/input.csv --output target/lab01/report.txt
java -cp target/classes ua.lpnu.lendiel.vitalii.lab01.Lab01Application --version
```

За замовчуванням використовується `data/input.csv`; якщо його немає, запуск
із репозиторію або JAR використовує classpath-файл `Data.csv`. Звіт за
замовчуванням записується до `target/lab01/report.txt`.

## Збірка і тестування

```bash
./mvnw clean test
./mvnw package
```

Тести перевіряють валідні, граничні та змішані рядки, фізичні номери помилок,
UTF-8-звіт і CLI-параметри.
