package ua.lpnu.lendiel.vitalii.lab05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Reads CSV files produced by {@link CsvExporter}.
 */
public final class CsvImporter {
    private CsvImporter() {
    }

    /**
     * Converts each data row with the supplied factory after checking the CSV
     * header against the annotated type.
     *
     * @param path input CSV path
     * @param type annotated object type represented by the file
     * @param rowFactory conversion from parsed fields to an object
     * @param <T> object type
     * @return immutable list of imported objects
     * @throws DataStorageException if reading, parsing, header validation, or
     *                              row conversion fails
     */
    public static <T> List<T> read(Path path, Class<T> type, RowFactory<T> rowFactory)
            throws DataStorageException {
        Objects.requireNonNull(path, "CSV input path cannot be null");
        Objects.requireNonNull(type, "CSV type cannot be null");
        Objects.requireNonNull(rowFactory, "CSV row factory cannot be null");

        final String document;
        try {
            document = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Could not read CSV file: " + path, exception);
        }

        List<List<String>> rows = CsvParser.parseDocument(document);
        if (rows.isEmpty()) {
            return List.of();
        }

        List<String> expectedHeader = CsvExporter.columnHeaders(type);
        if (!expectedHeader.equals(rows.get(0))) {
            throw new DataStorageException("CSV header does not match " + type.getName());
        }

        java.util.ArrayList<T> imported = new java.util.ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            if (rows.get(index).size() != expectedHeader.size()) {
                throw new DataStorageException("CSV row " + (index + 1)
                        + " must contain exactly " + expectedHeader.size() + " fields");
            }
            try {
                imported.add(Objects.requireNonNull(rowFactory.map(rows.get(index)),
                        "CSV row factory returned null"));
            } catch (DataStorageException exception) {
                throw exception;
            } catch (RuntimeException exception) {
                throw new DataStorageException("Could not convert CSV row "
                        + (index + 1), exception);
            }
        }
        return List.copyOf(imported);
    }

    /**
     * Converts one parsed CSV row into a domain object.
     *
     * @param <T> object type
     */
    @FunctionalInterface
    public interface RowFactory<T> {
        /**
         * Converts fields in exporter order.
         *
         * @param fields parsed row fields
         * @return converted object
         * @throws DataStorageException if the row is invalid
         */
        T map(List<String> fields) throws DataStorageException;
    }
}
