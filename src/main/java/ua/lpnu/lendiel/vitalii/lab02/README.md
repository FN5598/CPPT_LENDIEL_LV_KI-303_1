# Лабораторна робота № 2

## Статус

Лабораторна робота містить Java-програму для структурованого читання, перевірки та узагальнення даних про лікарські засоби.

## Код і ресурси

- Код: [`Lab02Application.java`](Lab02Application.java)
- Вхідні дані: [`Data.csv`](../../../../../../resources/lab01/Data.csv)
- Звіт: [`REPORT.md`](REPORT.md)

Програма перетворює кожен коректний CSV-рядок на публічний незмінний об'єкт
`MedicineInformation`. Його конструктор і `fromCsv(...)` застосовують однакові
інваріанти, а результати зберігаються в незмінному записі
`MedicineInformationSummary`. Перевіряються також порожні й нескінченні
числові значення.

## Збірка і тестування

```bash
./mvnw clean test
./mvnw package
java -cp target/classes ua.lpnu.lendiel.vitalii.lab02.Lab02Application
java -cp target/classes ua.lpnu.lendiel.vitalii.lab02.Lab02Application --version
```

Тести покривають конструктор, фабрику, додатні, граничні та від'ємні значення,
запис підсумку, classpath-ресурс і консольний запуск.
