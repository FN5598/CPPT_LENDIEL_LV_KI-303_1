# Report for Laboratory Work 3

## Topic and variant

- Domain: pharmacy and medicines.
- Variant: 11.
- Base type: `Medicine`.
- Subtypes: `PrescriptionMedicine`, `FreeMedicine`.
- Enum: `MedicineKind` (`PRESCRIPTION`, `FREE_SALE`).
- Polymorphic operation: `isAvailable(boolean prescriptionProvided)`.

## Implementation

The class model is represented by the following hierarchy:

```text
Medicine (abstract)
├── PrescriptionMedicine
└── FreeMedicine
```

`Medicine` contains the fields and invariants common to all medicines:
`name`, `form`, `price`, `daysToExpire`, the prescription boolean, and
`MedicineKind`. It validates the name, medicine form, price, expiration period,
and consistency between the category and prescription flag.

`PrescriptionMedicine` requires a prescription and a non-expired medicine for
`isAvailable(...)` to return `true`. `FreeMedicine` does not require a
prescription but still rejects an expired medicine. These different behaviors
are selected by dynamic dispatch through the `Medicine` reference.

`MedicineKind` provides the fixed categories `PRESCRIPTION` and `FREE_SALE`.
The form remains the separate `Pills` or `Liquid` field. `MedicineFactory`
converts the existing CSV boolean field into the matching subtype and retains
the boolean in the base `Medicine` object through `getIsPrescription()`, keeping
the input format unchanged.

The application processes all valid records as `List<Medicine>`. Its report
logic calculates the same average price, shortest expiration period,
prescription count, and valid-row count as Lab 02 without checking concrete
subtypes.

## Equality and collections

`equals()` and `hashCode()` are implemented in the base class using the
concrete type and all shared identity fields. Equal medicine objects therefore
behave consistently in `HashSet` and `HashMap` collections. The test suite
verifies that duplicate objects occupy one set entry and that objects with
different values do not compare equal.

## Inheritance decision

Inheritance is appropriate because both subtypes are medicines with the same
validated data and the same external representation. Their dispensing rules
are different implementations of one meaningful operation, so the common
abstract type allows the application to remain open for additional medicine
types without changing report generation.

## Compatibility

The original resource `src/main/resources/lab01/Data.csv`, its five-field
format, and the Lab 02 summary labels remain unchanged. Lab 02 tests are kept,
and Lab 03 adds tests for the base contract, both subtypes, the enum, factory
validation, polymorphism, equality, classpath loading, and report output.

## Build and test commands

```bash
./mvnw clean test
./mvnw verify
```

The supplied data produces the following compatible values:

- average medicine price: `3.64`;
- shortest medicine expiration period: `1`;
- prescription medicines: `3`;
- valid rows: `5`;
- invalid rows: `1`.

The command `./mvnw clean verify` completed successfully with 18 tests and no
SpotBugs findings. Javadoc generation with `./mvnw javadoc:javadoc` also
completed successfully.
