package ua.lpnu.lendiel.vitalii.lab05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ua.lpnu.lendiel.vitalii.LabCli;
import ua.lpnu.lendiel.vitalii.VersionInfo;
import ua.lpnu.lendiel.vitalii.lab03.Medicine;
import ua.lpnu.lendiel.vitalii.lab03.MedicineFactory;

/**
 * Demonstrates generic storage and reflective CSV round-trip persistence.
 */
public final class Lab05Application {
    private static final String DATA_CSV_PATH = "/lab01/Data.csv";
    private static final Path DEFAULT_OUTPUT = Path.of("target", "lab05", "medicines.csv");

    private Lab05Application() {
    }

    /**
     * Loads existing medicines, stores flat records, exports and imports them,
     * then verifies equality of the restored records.
     *
     * @param args optional first argument selecting the output CSV path
     */
    public static void main(String[] args) {
        final LabCli options;
        final Path output;
        try {
            options = LabCli.parse(args);
            if (options.positional().size() > 1
                    || (options.output() != null && !options.positional().isEmpty())) {
                throw new IllegalArgumentException("choose one output path");
            }
            if (options.help()) {
                System.out.println("Usage: lab05 [--input PATH] [--output PATH] "
                        + "[--help] [--version] [OUTPUT_CSV]\n"
                        + "Default input: bundled Data.csv\n"
                        + "Default output: " + DEFAULT_OUTPUT);
                return;
            }
            if (options.version()) {
                System.out.println(VersionInfo.labVersion("lab05"));
                return;
            }
            output = options.output() != null ? options.output()
                    : options.positional().isEmpty() ? DEFAULT_OUTPUT
                    : Path.of(options.positional().get(0));
        } catch (IllegalArgumentException exception) {
            System.err.println("Argument error: " + exception.getMessage());
            return;
        }

        try {
            String[] lines = options.readLines(Lab05Application.class, DATA_CSV_PATH);
            List<Medicine> medicines = new ArrayList<>();
            for (int index = 0; index < lines.length; index++) {
                try {
                    medicines.add(MedicineFactory.fromCsv(lines[index]));
                } catch (IllegalArgumentException exception) {
                    System.err.println("Row %d: %s".formatted(index + 1,
                            exception.getMessage()));
                }
            }

            Repository<MedicineRecord> repository = new Repository<>();
            medicines.stream()
                    .map(MedicineRecordMapper::fromMedicine)
                    .forEach(repository::add);

            Path absoluteOutput = output.toAbsolutePath();
            Path parent = absoluteOutput.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            CsvExporter.write(output, MedicineRecord.class, repository.all());
            List<MedicineRecord> restored = CsvImporter.read(output, MedicineRecord.class,
                    MedicineRecord::fromFields);
            List<Medicine> restoredMedicines = restored.stream()
                    .map(MedicineRecordMapper::toMedicine)
                    .toList();

            System.out.println("Repository records: " + repository.all().size());
            System.out.println("Exported CSV: " + output);
            System.out.println("Imported records: " + restored.size());
            System.out.println("Restored medicines: " + restoredMedicines.size());
            System.out.println("Round-trip equal: " + repository.all().equals(restored));
        } catch (IOException | DataStorageException | RuntimeException exception) {
            System.err.println("Lab 5 storage error: " + exception.getMessage());
        }
    }
}
