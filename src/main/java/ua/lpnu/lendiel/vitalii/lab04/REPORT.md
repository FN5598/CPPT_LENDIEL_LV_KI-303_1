# Report for Laboratory Work 4

## Topic and variant

- Domain: pharmacy and medicines.
- Variant: 11.
- Main topic: processing the medicine collection with the Java Stream API.
- Entry point: `ua.lpnu.lendiel.vitalii.lab04.Lab04Application`.

## Purpose

The purpose of this work is to process valid medicine objects with stream
operations instead of manually iterating over the collection. The application
also has to preserve errors for malformed CSV rows and continue processing the
valid rows.

## Program structure and data flow

The input file is loaded as UTF-8 text by `Lab04Application.getData(...)`.
`IntStream.range(...)` preserves the original row number while parsing. Each
row becomes a `ParseResult` containing either a `Medicine` object or an error
message.

The valid results are converted to `List<Medicine>`. This common collection is
used for all following operations. Invalid results are converted to formatted
messages such as `Row 3: ...` and printed in the error section.

```text
Data.csv
  → getData
  → parse rows with MedicineFactory
  → ParseResult objects
  → valid medicines and errors
  → Stream API calculations
  → console report
```

## Stream operations

Each required query is implemented by a separate named method:

1. `countExpiringWithinThirtyDays` uses `filter` to select medicines with
   `expirationDays <= 30`.
2. `mapMedicineNames` uses `map` to convert medicines to their names.
3. `countByForm` uses `Collectors.groupingBy` with `Collectors.counting()` to
   count medicines by `MedicineForm`.
4. `summarizePrices` uses `Collectors.summarizingDouble` to calculate price
   count, sum, average, minimum, and maximum.
5. `topFiveByExpiration` uses `sorted` with expiration as the primary
   criterion, name as the secondary criterion, and `limit(5)`.
6. `findByName` uses `filter` and `findFirst` and returns an
   `Optional<Medicine>` when no match exists.

Additional summary helpers calculate the shortest expiration period and the
prescription count. The input list is never modified by any query.

No concrete subtype checks are used in these operations. All processing is
performed through the common `Medicine` type and its public methods.

## Error handling

Parsing is performed inside the stream mapping operation. If
`MedicineFactory.fromCsv(...)` throws a runtime validation exception, the row
is represented by an invalid `ParseResult` instead of terminating the whole
pipeline. Valid rows remain available for the report.

The production dataset contains ten valid rows. Malformed input is tested by
passing an invalid CSV row through the Lab 04 parsing pipeline; the invalid row
becomes an error result while valid rows remain available for processing.

## Results

For the supplied dataset, the application produces:

| Calculation | Result |
|---|---:|
| Average medicine price | `4.44` |
| Shortest expiration period | `1` |
| Prescription medicines | `6` |
| Correct rows | `10` |
| Error rows | `0` |
| Medicines expiring within 30 days | `9` |
| Price count | `10` |
| Price sum | `44.40` |
| Average price | `4.44` |
| Minimum price | `3.00` |
| Maximum price | `6.20` |
| Pills | `4` |
| Liquid medicines | `6` |

The five medicines with the shortest expiration periods are:

```text
{Pakistani, Indian, Infinity, Alpha, Beta}
```

The name list preserves the CSV order and includes both records named
`Indian`.

## Comparison with the loop implementation

The five shared summary values are compared automatically in
`Lab04ApplicationTest` with the loop-based `Lab03Application`: average price,
shortest expiration period, prescription count, valid-row count, and error
count. Both implementations produce:

```text
Average medicine price: 4.44
Shortest medicine expiration period: 1
Total medicines that had prescription: 6
Total correct rows: 10
Errors: 0
```

## Testing

`Lab04ApplicationTest` verifies:

- loading the ten-row CSV resource;
- missing-resource errors;
- valid and invalid `ParseResult` values;
- summary statistics;
- expiration filtering;
- formatted name lists;
- grouping by medicine form;
- top-five sorting and tie-breaking;
- name lookup through a command-line argument;
- all named queries with empty input;
- top-N negative, zero, and oversized boundaries plus deliberate empty numeric
  summary formatting;
- comparison of the Stream API summary with the Lab 03 loop summary.

The complete test command is:

```bash
./mvnw clean test
```

The complete project suite currently contains 47 tests and executes with zero
failures and zero errors.

## Build and verification

The project uses Maven with Java 21, JUnit Jupiter, and SpotBugs. The relevant
commands are:

```bash
./mvnw clean test
./mvnw verify
./mvnw package
```

`verify` runs the tests, creates the JAR, and checks the compiled classes with
SpotBugs. The GitHub Actions workflow repeats `verify` on Ubuntu, Windows, and
macOS and uploads the JAR produced on each operating system.

## Documentation

Javadoc describes the public `Lab04Application` class, its `main` method, and
the `getData` method. The README documents the data flow, stream operations,
commands, and expected output.

## Conclusion

The laboratory work demonstrates how the common medicine model can be
processed with independent Stream API operations. The `ParseResult` structure
keeps parsing errors separate from valid domain objects, while filtering,
mapping, grouping, sorting, limiting, and lookup are performed without manual
type-based branching.
