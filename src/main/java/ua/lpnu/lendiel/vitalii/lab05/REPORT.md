# Report for Laboratory Work 5

## Topic and variant

- Domain: pharmacy and medicines.
- Variant: 11.
- Topic: generic storage, annotations, reflection, CSV export/import, and
  round-trip verification.

## Purpose

The work adds reusable persistence infrastructure without coupling the generic
repository or exporter to concrete medicine classes. Existing Lab 3/4 objects
are converted to a flat `MedicineRecord`, written to CSV, read back, and
compared with `equals`.

## Implementation

`Repository<T>` stores any non-null type. Its `all()` method returns an
immutable snapshot, and `find(Predicate<? super T>)` returns a new immutable
result list. The repository does not know about medicines.

`@CsvColumn` has `RUNTIME` retention and `FIELD` target. `CsvExporter` locates
annotated non-static fields with reflection, calls `trySetAccessible()`, sorts
fields by Java name, writes annotation values as the header, and escapes CSV
special characters. `CsvImporter` reads UTF-8, validates the header, and maps
each row through a caller-supplied factory.

`CsvParser` is a quote-aware state machine. It recognizes commas and line
breaks as separators only outside quoted fields and converts doubled quotes to
one quote. Malformed quotes produce `DataStorageException`.

`MedicineRecord` contains the required flat fields:

```text
name, form, price, expiresOn, prescription
```

The actual deterministic exporter order is
`expiresOn, form, name, prescription, price`, because fields are sorted by Java
field name. `MedicineRecordMapper` uses `2026-01-01` as a stable anchor to map
the previous model's `expirationDays` to and from `expiresOn`. The form switch
restores either `PillsMedicine` or `LiquidMedicine`.

## Data flow

```text
Data.csv
  → Lab 3 MedicineFactory
  → List<Medicine>
  → MedicineRecordMapper
  → Repository<MedicineRecord>
  → CsvExporter
  → medicines.csv
  → CsvImporter and MedicineRecord.fromFields
  → List<MedicineRecord>
  → equals comparison and subtype restoration
```

## Results

Running the application with the supplied ten-row dataset produces:

```text
Repository records: 10
Imported records: 10
Restored medicines: 10
Round-trip equal: true
```

The output CSV is UTF-8 and contains the Ukrainian annotated header:

```text
придатність,форма,назва,рецепт,ціна
```

## Testing

The combined suite currently contains 47 tests, covering positive, boundary,
and negative cases. Lab 5 additionally verifies inherited annotated fields and
rejects rows whose field count does not match the header.

The test suite checks repository behavior, annotation retention, inherited and
deterministic headers, empty exports, special-character escaping, malformed
quotes, invalid
record fields, empty and header-only files, wrong headers, missing files,
unwritable paths, preserved causes, equality of restored data, and restoration
of both medicine subtypes.

## Commands and infrastructure

```bash
./mvnw test
./mvnw verify
./mvnw package
java -jar target/CPPT_LAB_WORKS-5.0.0.jar
```

`verify` executes JUnit and SpotBugs. The Maven JAR manifest now names
`ua.lpnu.lendiel.vitalii.lab05.Lab05Application` as `Main-Class`, so the
packaged artifact is directly executable. CI repeats `verify` on Ubuntu,
Windows, and macOS and uploads the JAR artifact.

## Academic integrity

The implementation was developed with Codex assistance for planning,
explanation, code review, and test-case suggestions. Accepted suggestions were
the generic repository boundary, runtime field annotation, quote-aware parser,
cause-preserving checked exception, round-trip test, and executable JAR
manifest. Suggestions were reviewed against the existing model and adapted
where necessary: the old model uses `expirationDays`, so a fixed reference date
was introduced instead of using the current date and making tests time
dependent. The final code, tests, commands, and results were checked locally.

## Conclusion

Lab 5 adds reusable generic and reflection infrastructure while preserving the
previous polymorphic domain model. Equality-based round-trip testing confirms
that exported records can be restored without data loss. The repository,
annotation, parser, importer, and exception boundaries provide the foundation
for future persistence features.
