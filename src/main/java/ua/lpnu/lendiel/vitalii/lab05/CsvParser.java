package ua.lpnu.lendiel.vitalii.lab05;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Parses comma-separated data including quoted commas, quotes, and newlines.
 */
public final class CsvParser {
    private CsvParser() {
    }

    /**
     * Parses a complete CSV document using a small quote-aware state machine.
     *
     * @param document UTF-8 CSV text
     * @return rows and fields in their original order
     * @throws DataStorageException if quotes are malformed
     */
    public static List<List<String>> parseDocument(String document)
            throws DataStorageException {
        Objects.requireNonNull(document, "CSV document cannot be null");
        if (document.isEmpty()) {
            return List.of();
        }

        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean fieldStarted = false;
        boolean afterClosingQuote = false;

        for (int index = 0; index < document.length(); index++) {
            char current = document.charAt(index);

            if (quoted) {
                if (current == '"') {
                    if (index + 1 < document.length()
                            && document.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                        afterClosingQuote = true;
                    }
                } else {
                    field.append(current);
                }
                continue;
            }

            if (current == '"') {
                if (fieldStarted) {
                    throw malformed(index, "quote must start a field");
                }
                quoted = true;
                fieldStarted = true;
            } else if (current == ',') {
                if (afterClosingQuote) {
                    afterClosingQuote = false;
                }
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
            } else if (current == '\n' || current == '\r') {
                if (afterClosingQuote) {
                    afterClosingQuote = false;
                }
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
                rows.add(List.copyOf(row));
                row.clear();
                if (current == '\r' && index + 1 < document.length()
                        && document.charAt(index + 1) == '\n') {
                    index++;
                }
            } else {
                if (afterClosingQuote) {
                    throw malformed(index, "characters after a closing quote");
                }
                field.append(current);
                fieldStarted = true;
            }
        }

        if (quoted) {
            throw malformed(document.length(), "unclosed quoted field");
        }

        if (fieldStarted || !row.isEmpty() || afterClosingQuote) {
            row.add(field.toString());
            rows.add(List.copyOf(row));
        }

        return List.copyOf(rows);
    }

    private static DataStorageException malformed(int position, String reason) {
        return new DataStorageException("Malformed CSV at character " + position
                + ": " + reason);
    }
}
