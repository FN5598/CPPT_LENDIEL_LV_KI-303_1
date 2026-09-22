# Laboratory Work 5

## Topic and variant

This laboratory work extends the pharmacy domain from variant 11 with generic
storage and reflection-based CSV persistence. The previous `Medicine` model and
all previous tests remain unchanged.

## Structure

```text
Medicine objects from Lab 3/4
        ↓ MedicineRecordMapper
Repository<MedicineRecord>
        ↓ CsvExporter + @CsvColumn reflection
UTF-8 CSV file
        ↓ CsvParser + CsvImporter
MedicineRecord objects
        ↓ equals/hashCode
round-trip verification
```

The Lab 5 package contains:

- `Repository<T>` — generic in-memory storage with snapshot and predicate search;
- `CsvColumn` — runtime field annotation for CSV headers;
- `CsvExporter` — generic reflection-based exporter;
- `CsvParser` — quote-aware CSV state-machine parser;
- `CsvImporter` — header validation and row conversion;
- `MedicineRecord` — flat representation with `name`, `form`, `price`,
  `expiresOn`, and `prescription`;
- `MedicineRecordMapper` — conversion to and from the existing polymorphic
  `Medicine` model;
- `DataStorageException` — checked exception that preserves original causes;
- `Lab05Application` — executable round-trip demonstration.

## CSV format

The exporter sorts annotated fields by Java field name. Therefore the generated
header is deterministic:

```text
придатність,форма,назва,рецепт,ціна
```

The parser supports commas, doubled quotes, and line breaks inside quoted
fields. It does not use `split(",")`.

`Medicine` stores expiration as a number of days, while `MedicineRecord` stores
an absolute `LocalDate`. The mapper uses the stable anchor date
`2026-01-01`, so conversion is independent of the day on which the program is
run and preserves the previous model's integer expiration value.

## Build and run

```bash
./mvnw clean test
./mvnw verify
./mvnw package
java -jar target/CPPT_LAB_WORKS-5.0.0.jar
```

An optional first argument changes the output path:

```bash
java -jar target/CPPT_LAB_WORKS-5.0.0.jar target/lab05/custom.csv
```

The default output is `target/lab05/medicines.csv`. A successful run prints the
repository size, export path, imported size, restored medicine count, and
`Round-trip equal: true`.

The executable JAR also supports `--help` and `--version`.

## Testing

The Lab 5 tests cover:

- repository insertion, predicate search, null rejection, and snapshots;
- runtime annotation discovery;
- deterministic headers and empty exports;
- comma, quote, and multiline CSV escaping;
- malformed quotes;
- valid and invalid date, enum, number, boolean, and field-count values;
- empty files, header-only files, wrong headers, missing files, and unwritable
  paths;
- preservation of I/O and conversion causes;
- equality-based export/import round trips;
- restoration of both `PillsMedicine` and `LiquidMedicine` subtypes.

## CI

GitHub Actions runs `./mvnw -B verify` on Ubuntu, Windows, and macOS with Java
21 and uploads the generated executable JAR as an artifact.
