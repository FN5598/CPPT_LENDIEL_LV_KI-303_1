package ua.lpnu.lendiel.vitalii.lab05;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import ua.lpnu.lendiel.vitalii.VersionInfo;
import ua.lpnu.lendiel.vitalii.lab03.Lab03Application;
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
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            System.out.println("Usage: lab05 [OUTPUT_CSV]\n"
                    + "Default output: " + DEFAULT_OUTPUT);
            return;
        }
        if (args.length == 1 && "--version".equals(args[0])) {
            System.out.println(VersionInfo.labVersion("lab05"));
            return;
        }
        if (args.length > 1) {
            System.err.println("Usage: lab05 [OUTPUT_CSV]");
            return;
        }
        Path output = args.length == 0 ? DEFAULT_OUTPUT : Path.of(args[0]);
        try {
            List<Medicine> medicines = Arrays.stream(
                    Lab03Application.getData(DATA_CSV_PATH))
                    .map(MedicineFactory::fromCsv)
                    .toList();

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
