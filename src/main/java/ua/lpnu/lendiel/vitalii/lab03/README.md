# Laboratory Work 3

## Topic and variant

This laboratory work extends the medicine domain model for variant 11 using
inheritance, an enum for medicine form, polymorphism, and value-based equality.

## Class model

```text
Medicine (abstract)
├── PillsMedicine
└── LiquidMedicine

MedicineForm (enum)
```

`Medicine` stores the common fields `name`, `form`, `price`,
`expirationDays`, and the prescription boolean from the CSV row. `MedicineForm`
represents the physical form as `PILLS` or `LIQUID`.

`PillsMedicine` and `LiquidMedicine` set the form-specific enum value through
the base constructor. The common `Medicine` implementation handles the
prescription rule, so the subclasses do not duplicate prescription checks.
The application processes both subtypes through `List<Medicine>`.

`MedicineFactory` uses the form value to select `PillsMedicine` or
`LiquidMedicine` and passes the final CSV boolean into the base `Medicine`
object.

## Input data

The input resource is [`Data.csv`](../../../../../../resources/lab01/Data.csv)
with this format:

```text
name;form;price;expirationDays;prescription
```

The dataset contains ten valid records and covers both forms, both subtypes,
expiration boundaries at 30 and 31 days, duplicate names, and different prices.
Malformed rows are tested directly through `MedicineFactory` instead of being
included in the production resource.

## Build and run

```bash
./mvnw clean test
./mvnw verify
java -cp target/classes ua.lpnu.lendiel.vitalii.lab03.Lab03Application
```

Expected statistics for the supplied data are an average price of `4.44`, a
shortest expiration period of `1`, six prescription medicines, ten valid rows,
and zero invalid rows.
