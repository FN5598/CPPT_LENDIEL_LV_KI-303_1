# Laboratory Work 3

## Topic and variant

This laboratory work extends the medicine domain model for variant 11 using
inheritance, an enum, polymorphism, and value-based equality.

## Class model

```text
Medicine (abstract)
├── PrescriptionMedicine
└── FreeMedicine

MedicineKind (enum)
```

`Medicine` stores the shared fields `name`, `form`, `price`, and
`daysToExpire`, together with the prescription flag and `MedicineKind` category.
It enforces the common validation rules.
`PrescriptionMedicine` and `FreeMedicine` provide different implementations of
`isAvailable(boolean prescriptionProvided)`.

The application stores both subtypes in `List<Medicine>`. Report generation
uses only the common type and does not branch on a concrete subtype. Adding a
new subtype therefore does not require rewriting the reporting code.

`MedicineKind` represents the prescription category with explicit values
`PRESCRIPTION` and `FREE_SALE`. The form remains the separate `Pills` or
`Liquid` field. The final CSV field remains the `true`/`false` prescription flag
and is stored in the base `Medicine` object, so the format remains compatible
with Lab 02.

`Medicine.equals()` and `Medicine.hashCode()` use the concrete type and all
shared identity fields. This keeps equal objects interchangeable in
`HashSet` and `HashMap` collections.

## Input and output compatibility

The input resource remains [`Data.csv`](../../../../../../resources/lab01/Data.csv)
with this format:

```text
name;form;price;daysToExpire;prescription
```

The summary labels and calculated values remain compatible with Lab 02.

## Build and run

```bash
./mvnw clean test
./mvnw verify
java -cp target/classes ua.lpnu.lendiel.vitalii.lab03.Lab03Application
```

Expected statistics for the supplied data are an average price of `3.64`, a
shortest expiration period of `1`, three prescription medicines, five valid
rows, and one invalid row.
