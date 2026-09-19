# Report for Laboratory Work 3

## Topic and variant

- Domain: pharmacy and medicines.
- Variant: 11.
- Base type: `Medicine`.
- Subtypes: `PillsMedicine`, `LiquidMedicine`.
- Enum: `MedicineForm` (`PILLS`, `LIQUID`).
- Polymorphic operation: `getForm()`.

## Implementation

The hierarchy is:

```text
Medicine (abstract)
├── PillsMedicine
└── LiquidMedicine
```

`Medicine` contains the shared fields and invariants: `name`, `form`, `price`,
`expirationDays`, and the prescription boolean from the CSV row. The string
form is converted to `MedicineForm` at the CSV boundary.

`PillsMedicine` and `LiquidMedicine` implement `getForm()` for their physical
forms. The common `Medicine` implementation handles prescription and expiration
checks, so the subclasses do not duplicate that logic.

`MedicineFactory` parses the five-column CSV format. The final boolean selects
the prescription behavior and is stored in the created object. The form selects
the concrete form subtype. Invalid rows fail before an object can be returned.

## Equality and collections

`equals()` and `hashCode()` use the concrete type and all common fields:
`name`, `form`, `price`, `expirationDays`, and `prescription`. Equal objects
therefore behave consistently in `HashSet` and `HashMap` collections.

## Compatibility and results

The CSV format remains five fields wide:

```text
name;form;price;expirationDays;prescription
```

The expanded resource contains ten valid rows. Its summary is:

- average medicine price: `4.44`;
- shortest expiration period: `1`;
- prescription medicines: `6`;
- valid rows: `10`;
- invalid rows: `0`.

Malformed input is covered by factory tests rather than the production dataset.

## Build and test commands

```bash
./mvnw clean test
./mvnw verify
```

The build compiles the application and tests, runs the full test suite, and
checks the compiled classes with SpotBugs.
