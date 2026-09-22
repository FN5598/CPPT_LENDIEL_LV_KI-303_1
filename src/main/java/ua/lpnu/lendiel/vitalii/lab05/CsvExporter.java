package ua.lpnu.lendiel.vitalii.lab05;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Exports arbitrary annotated objects to UTF-8 CSV using reflection.
 */
public final class CsvExporter {
    private CsvExporter() {
    }

    /**
     * Writes a CSV header and all supplied objects.
     *
     * <p>Columns are sorted by Java field name to make output deterministic.
     * Values containing a comma, quote, carriage return, or newline are quoted
     * according to standard CSV escaping rules.</p>
     *
     * @param path output path
     * @param type concrete object class whose fields are exported
     * @param records objects to export
     * @param <T> object type
     * @throws DataStorageException if reflection, validation, or file writing
     *                              fails
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        Objects.requireNonNull(path, "CSV output path cannot be null");
        Objects.requireNonNull(type, "CSV type cannot be null");
        Objects.requireNonNull(records, "CSV records cannot be null");

        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new DataStorageException("No fields annotated with @CsvColumn in "
                    + type.getName());
        }

        try {
            fields.forEach(CsvExporter::makeAccessible);
            StringBuilder document = new StringBuilder();
            appendRow(document, fields.stream()
                    .map(field -> field.getAnnotation(CsvColumn.class).value())
                    .toList());

            for (T record : records) {
                if (record == null) {
                    throw new DataStorageException("CSV records cannot contain null");
                }
                List<String> values = fields.stream()
                        .map(field -> readField(field, record))
                        .toList();
                appendRow(document, values);
            }

            Files.writeString(path, document.toString(), StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException exception) {
            throw new DataStorageException("Could not write CSV file: " + path, exception);
        }
    }

    static List<Field> annotatedFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers())
                        && field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        return fields.stream().sorted(Comparator.comparing(Field::getName)).toList();
    }

    static List<String> columnHeaders(Class<?> type) throws DataStorageException {
        List<Field> fields = annotatedFields(type);
        if (fields.isEmpty()) {
            throw new DataStorageException("No fields annotated with @CsvColumn in "
                    + type.getName());
        }
        return fields.stream()
                .map(field -> field.getAnnotation(CsvColumn.class).value())
                .toList();
    }

    private static void makeAccessible(Field field) {
        if (!field.trySetAccessible()) {
            throw new IllegalStateException("Cannot access field " + field.getName());
        }
    }

    private static String readField(Field field, Object record) {
        try {
            return Objects.toString(field.get(record), "");
        } catch (IllegalAccessException | IllegalArgumentException exception) {
            throw new IllegalStateException("Cannot read field " + field.getName(), exception);
        }
    }

    private static void appendRow(StringBuilder document, List<String> values) {
        if (!document.isEmpty()) {
            document.append('\n');
        }
        document.append(String.join(",", values.stream().map(CsvExporter::escape).toList()));
    }

    private static String escape(String value) {
        boolean needsQuotes = value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0;
        if (!needsQuotes) {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }
}
