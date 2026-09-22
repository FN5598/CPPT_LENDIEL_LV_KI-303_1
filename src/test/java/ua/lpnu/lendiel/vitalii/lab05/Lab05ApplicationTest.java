package ua.lpnu.lendiel.vitalii.lab05;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ua.lpnu.lendiel.vitalii.lab03.LiquidMedicine;
import ua.lpnu.lendiel.vitalii.lab03.Medicine;
import ua.lpnu.lendiel.vitalii.lab03.MedicineFactory;
import ua.lpnu.lendiel.vitalii.lab03.MedicineForm;
import ua.lpnu.lendiel.vitalii.lab03.PillsMedicine;

class Lab05ApplicationTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void repositoryStoresFindsAndProtectsItsState() {
        Repository<String> repository = new Repository<>();
        repository.add("Aspirin");
        repository.add("Vitamin");

        assertEquals(List.of("Aspirin", "Vitamin"), repository.all());
        assertEquals(List.of("Vitamin"), repository.find(value -> value.startsWith("V")));
        assertEquals(repository.all(), repository.snapshot());
        assertEquals(List.of("Vitamin"), repository.search(value -> value.startsWith("V")));
        assertThrows(UnsupportedOperationException.class, () -> repository.all().add("X"));
        assertThrows(NullPointerException.class, () -> repository.add(null));
        assertThrows(NullPointerException.class, () -> repository.find(null));
    }

    @Test
    void csvColumnIsAvailableAtRuntimeOnFields() throws NoSuchFieldException {
        assertEquals(java.lang.annotation.RetentionPolicy.RUNTIME,
                CsvColumn.class.getAnnotation(java.lang.annotation.Retention.class).value());
        assertEquals("назва", SampleRow.class.getDeclaredField("name")
                .getAnnotation(CsvColumn.class).value());
    }

    @Test
    void exporterSortsColumnsAndEscapesSpecialValues() throws Exception {
        Path path = temporaryDirectory.resolve("sample.csv");
        CsvExporter.write(path, SampleRow.class, List.of(
                new SampleRow("A, B \"special\"\nnext", "Alpha")));

        assertEquals("опис,назва\n\"A, B \"\"special\"\"\nnext\",Alpha",
                Files.readString(path, StandardCharsets.UTF_8));
    }

    @Test
    void exporterWritesHeaderForEmptyList() throws Exception {
        Path path = temporaryDirectory.resolve("empty.csv");

        CsvExporter.write(path, SampleRow.class, List.of());

        assertEquals("опис,назва", Files.readString(path, StandardCharsets.UTF_8));
    }

    @Test
    void exporterRejectsClassesWithoutAnnotatedFields() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(temporaryDirectory.resolve("plain.csv"),
                        PlainRow.class, List.of(new PlainRow())));

        assertTrue(exception.getMessage().contains("@CsvColumn"));
    }

    @Test
    void exporterPreservesWriteCause() throws IOException {
        Path directory = temporaryDirectory.resolve("not-a-file");
        Files.createDirectory(directory);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(directory, SampleRow.class, List.of()));

        assertNotNull(exception.getCause());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void parserHandlesCommaQuotesAndNewlines() throws Exception {
        List<List<String>> rows = CsvParser.parseDocument(
                "name,note\n\"Painkiller, Extra\",\"Medicine \"\"Plus\"\"\nsecond\"\n");

        assertEquals(List.of(
                List.of("name", "note"),
                List.of("Painkiller, Extra", "Medicine \"Plus\"\nsecond")), rows);
    }

    @Test
    void parserRejectsMalformedQuotes() {
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("name,note\n\"unclosed,value\n"));
        assertThrows(DataStorageException.class,
                () -> CsvParser.parseDocument("name,note\n\"quoted\"tail,value"));
    }

    @Test
    void medicineRecordValidatesAndParsesExporterOrder() throws Exception {
        MedicineRecord record = MedicineRecord.fromFields(List.of(
                "2026-01-31", "Pills", "Aspirin", "true", "12.50"));

        assertEquals("Aspirin", record.getName());
        assertEquals(MedicineForm.PILLS, record.getForm());
        assertEquals(12.5, record.getPrice());
        assertEquals(LocalDate.of(2026, 1, 31), record.getExpiresOn());
        assertTrue(record.requiresPrescription());
        assertThrows(DataStorageException.class,
                () -> MedicineRecord.fromFields(List.of("too", "few")));
        assertThrows(DataStorageException.class,
                () -> MedicineRecord.fromFields(List.of(
                        "2026-01-31", "Capsule", "Aspirin", "true", "12.50")));
        assertThrows(DataStorageException.class,
                () -> MedicineRecord.fromFields(List.of(
                        "2026-01-31", "Pills", "Aspirin", "maybe", "12.50")));
        assertThrows(DataStorageException.class,
                () -> MedicineRecord.fromFields(List.of(
                        "2026-01-31", "Pills", "Aspirin", "true", "not-a-number")));
        assertThrows(DataStorageException.class,
                () -> MedicineRecord.fromFields(List.of(
                        "not-a-date", "Pills", "Aspirin", "true", "12.50")));
    }

    @Test
    void importerAcceptsEmptyAndHeaderOnlyDocuments() throws Exception {
        Path empty = temporaryDirectory.resolve("empty.csv");
        Path headerOnly = temporaryDirectory.resolve("header.csv");
        Files.writeString(empty, "", StandardCharsets.UTF_8);
        Files.writeString(headerOnly, "придатність,форма,назва,рецепт,ціна",
                StandardCharsets.UTF_8);

        assertTrue(CsvImporter.read(empty, MedicineRecord.class,
                MedicineRecord::fromFields).isEmpty());
        assertTrue(CsvImporter.read(headerOnly, MedicineRecord.class,
                MedicineRecord::fromFields).isEmpty());
    }

    @Test
    void importerRejectsWrongHeaderAndPreservesMissingFileCause() throws Exception {
        Path wrongHeader = temporaryDirectory.resolve("wrong-header.csv");
        Files.writeString(wrongHeader, "name,form,price,expiresOn,prescription\n",
                StandardCharsets.UTF_8);

        assertThrows(DataStorageException.class,
                () -> CsvImporter.read(wrongHeader, MedicineRecord.class,
                        MedicineRecord::fromFields));

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(temporaryDirectory.resolve("missing.csv"),
                        MedicineRecord.class, MedicineRecord::fromFields));
        assertNotNull(exception.getCause());
    }

    @Test
    void exporterAndImporterPerformAnEqualMedicineRecordRoundTrip() throws Exception {
        List<MedicineRecord> original = List.of(
                new MedicineRecord("Painkiller, Extra", MedicineForm.LIQUID, 7.5,
                        LocalDate.of(2026, 11, 1), false),
                new MedicineRecord("Medicine \"Plus\"", MedicineForm.PILLS, 8.1,
                        LocalDate.of(2026, 12, 1), true));
        Path path = temporaryDirectory.resolve("medicines.csv");

        CsvExporter.write(path, MedicineRecord.class, original);
        List<MedicineRecord> restored = CsvImporter.read(path, MedicineRecord.class,
                MedicineRecord::fromFields);

        assertEquals(original, restored);
        assertEquals(original.hashCode(), restored.hashCode());
    }

    @Test
    void mapperPreservesPolymorphicMedicineAndCommonFields() {
        List<Medicine> original = List.of(
                new PillsMedicine("Aspirin", 12.5, 30, true),
                new LiquidMedicine("Vitamin", 5, 7, false));

        List<Medicine> restored = original.stream()
                .map(MedicineRecordMapper::fromMedicine)
                .map(MedicineRecordMapper::toMedicine)
                .toList();

        assertEquals(original, restored);
        assertTrue(restored.get(0) instanceof PillsMedicine);
        assertTrue(restored.get(1) instanceof LiquidMedicine);
        assertFalse(restored.get(1).requiresPrescription());
    }

    @Test
    void importerReportsInvalidDataRowWithOriginalCause() throws Exception {
        Path path = temporaryDirectory.resolve("invalid.csv");
        Files.writeString(path,
                "придатність,форма,назва,рецепт,ціна\n"
                        + "2026-01-31,Pills,Aspirin,true,broken\n",
                StandardCharsets.UTF_8);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(path, MedicineRecord.class,
                        MedicineRecord::fromFields));

        assertNotNull(exception.getCause());
    }

    @Test
    void exporterIncludesAnnotatedInheritedFieldsAndImporterRejectsWrongWidth() throws Exception {
        Path path = temporaryDirectory.resolve("inherited.csv");
        CsvExporter.write(path, DerivedRow.class, List.of(new DerivedRow("base", "child")));

        assertEquals("base-header,child-header\nbase,child",
                Files.readString(path, StandardCharsets.UTF_8));

        Path malformed = temporaryDirectory.resolve("wrong-width.csv");
        Files.writeString(malformed, "придатність,форма,назва,рецепт,ціна\n2026-01-01,Pills",
                StandardCharsets.UTF_8);
        assertThrows(DataStorageException.class,
                () -> CsvImporter.read(malformed, MedicineRecord.class,
                        MedicineRecord::fromFields));
    }

    private static final class SampleRow {
        @CsvColumn("назва")
        private final String name;

        @CsvColumn("опис")
        private final String description;

        private SampleRow(String description, String name) {
            this.description = description;
            this.name = name;
        }
    }

    private static final class PlainRow {
    }

    private static class BaseRow {
        @CsvColumn("base-header")
        private final String base;

        private BaseRow(String base) {
            this.base = base;
        }
    }

    private static final class DerivedRow extends BaseRow {
        @CsvColumn("child-header")
        private final String child;

        private DerivedRow(String base, String child) {
            super(base);
            this.child = child;
        }
    }
}
