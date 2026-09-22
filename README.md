# CPPT Laboratory Works

Лабораторні роботи з дисципліни «Кросплатформенні засоби програмування».

## Структура проєкту

```text
src/main/java/                       Java-код і документація лабораторних робіт
src/main/resources/                  CSV та інші ресурси програми
src/test/java/                       Модульні тести
src/main/java/.../lab01/             Код і документація лабораторної роботи № 1
src/main/java/.../lab02/             Код і документація лабораторної роботи № 2
src/main/java/.../lab03/             Код і документація лабораторної роботи № 3
src/main/java/.../lab04/             Код і документація лабораторної роботи № 4
src/main/java/.../lab05/             Код і документація лабораторної роботи № 5
REPORT.md                            Загальний звіт з посиланнями на звіти
pom.xml                              Конфігурація Maven
```

## Лабораторні роботи

| Робота | Опис                               | Документація              | Звіт                      |
| ------ | ---------------------------------- | ------------------------- | ------------------------- |
| 1      | Обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab01/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab01/REPORT.md) |
| 2      | Структурована обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab02/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab02/REPORT.md) |
| 3      | Наслідування та поліморфна модель лікарських засобів | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab03/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab03/REPORT.md) |
| 4      | Обробка колекції через Stream API | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab04/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab04/REPORT.md) |
| 5      | Generic repository і CSV persistence через reflection | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab05/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab05/REPORT.md) |

## Вимоги

- Java 21 або новіша;
- Maven Wrapper, який входить до репозиторію;
- Git для роботи з історією, Issues та Pull Requests.

## Збірка і тестування

Linux/macOS:

```bash
./mvnw clean test
./mvnw package
```

Windows:

```bat
mvnw.cmd clean test
mvnw.cmd package
```

## Ресурси

Файли, які не компілюються, розміщуються в `src/main/resources`. Наприклад, дані лабораторної роботи № 1 знаходяться в [Data.csv](src/main/resources/lab01/Data.csv).

## Звіти

Повний перелік звітів доступний у [REPORT.md](REPORT.md).

## Версії та релізи

Кожна лабораторна робота є послідовним релізом продукту:

| Лабораторна | Версія POM і `--version` | Git tag |
| --- | --- | --- |
| 1 | `1.0.0` | `v1.0.0` |
| 2 | `2.0.0` | `v2.0.0` |
| 3 | `3.0.0` | `v3.0.0` |
| 4 | `4.0.0` | `v4.0.0` |
| 5 | `5.0.0` | `v5.0.0` |

Поточна гілка містить кумулятивний реліз лабораторної роботи № 5. Для
перегляду попередніх релізів використовуйте відповідні release-гілки й теги.
Версію кожної програми можна перевірити аргументом `--version`.
