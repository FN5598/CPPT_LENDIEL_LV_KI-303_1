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
REPORT.md                            Загальний звіт з посиланнями на звіти
pom.xml                              Конфігурація Maven
```

## Лабораторні роботи

| Робота | Опис                               | Документація              | Звіт                      |
| ------ | ---------------------------------- | ------------------------- | ------------------------- |
| 1      | Обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab01/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab01/REPORT.md) |
| 2      | Структурована обробка даних про лікарські засоби | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab02/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab02/REPORT.md) |
| 3      | Наслідування та поліморфна модель лікарських засобів | [README](src/main/java/ua/lpnu/lendiel/vitalii/lab03/README.md) | [REPORT](src/main/java/ua/lpnu/lendiel/vitalii/lab03/REPORT.md) |

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
