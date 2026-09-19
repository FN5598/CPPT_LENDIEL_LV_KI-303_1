# Laboratory Work 4

## Topic and variant

This laboratory work applies the Stream API to the medicine domain for
variant 11. The application reads medicine records, keeps invalid-row errors,
and calculates collections and statistics from the valid medicines.

## Program structure

```text
Data.csv
   ↓
Lab04Application.getData(...)
   ↓
IntStream.range(...)
   ↓
ParseResult
   ├── valid Medicine objects
   └── error messages
   ↓
Stream API calculations and report output
```

The application uses the model from package `lab03`:

- `Medicine` is the common type;
- `PillsMedicine` and `LiquidMedicine` are the concrete types;
- `MedicineFactory` parses CSV rows;
- `MedicineForm` is used for grouping.

The Lab 04 entry point is [`Lab04Application.java`](Lab04Application.java).
The tests are in
[`Lab04ApplicationTest.java`](../../../../../../../test/java/ua/lpnu/lendiel/vitalii/lab04/Lab04ApplicationTest.java).

## Input and error handling

The input resource is [`Data.csv`](../../../../../../resources/lab01/Data.csv)
with the following format:

```text
name;form;price;expirationDays;prescription
```

Each row is parsed into a `ParseResult`. A valid result contains a `Medicine`;
an invalid result contains the row number and an error message. Invalid rows
do not stop processing of valid rows.

## Stream API operations

The application performs these operations:

- `filter` counts medicines expiring within 30 days;
- `map` creates a list of medicine names;
- `groupingBy` counts medicines by `MedicineForm`;
- `mapToDouble` and `average`-style aggregation calculate price statistics;
- `sorted`, `thenComparing`, and `limit(5)` select the five shortest expiration
  periods, using the name as a tie-breaker;
- `filter`, `map`, and `findFirst` implement a medicine-name lookup.

## Example output

```text
Average medicine price: 4.44
Shortest medicine expiration period: 1
Total medicines that had prescription: 6
Total correct rows: 10
Errors: 0
Medicines that expire within 30 days: 9
Names of all medicines: {Indian, Pakistani, Pantheon, Infinity, Doubledown, Decrease, Beyond, Alpha, Beta, Indian}
Top Five Medicine names with least expiration time: {Pakistani, Indian, Infinity, Alpha, Beta}
PILLS -> 4
LIQUID -> 6
```

## Build, test, and run

```bash
./mvnw clean test
./mvnw verify
./mvnw package
java -cp target/classes ua.lpnu.lendiel.vitalii.lab04.Lab04Application
```

To search for a medicine by name, pass the name as the first argument:

```bash
java -cp target/classes ua.lpnu.lendiel.vitalii.lab04.Lab04Application Indian
```

## Continuous integration

The GitHub Actions workflow runs `./mvnw -B verify` on Ubuntu, Windows, and
macOS with Java 21. A successful run uploads the generated JAR as a workflow
artifact.
