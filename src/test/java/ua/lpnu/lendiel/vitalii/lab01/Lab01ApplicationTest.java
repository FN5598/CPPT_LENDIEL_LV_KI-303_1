package ua.lpnu.lendiel.vitalii.lab01;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Lab01ApplicationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void loadsCsvDataFromResources() throws IOException {
        String[] data = Lab01Application.getData();
        assertEquals(6, data.length);
        assertTrue(data[0].startsWith("Indian;Pills"));
    }

    @Test
    void calculatesStatisticsAndReportsInvalidRows() {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            Lab01Application.main(new String[0]);
        } finally {
            System.setOut(originalOutput);
        }

        String result = output.toString(StandardCharsets.UTF_8);
        assertTrue(result.contains("Average price: 3.64"));
        assertTrue(result.contains("Prescription count: 3"));
        assertTrue(result.contains("Shortest expiration period: 1"));
        assertTrue(result.contains("Total Rows: 5"));
        assertTrue(result.contains("Line 6:"));
    }

    @Test
    void skipsEveryInvalidPhysicalLineAndKeepsItsReason() {
        String report = Lab01Application.buildReport(List.of(
                "Valid;Pills;10;0;true",
                "Broken;Capsule;99;99;false",
                "Too;few;columns",
                "Also;Liquid;-1;3;false"));

        assertTrue(report.contains("Total Rows: 1"));
        assertTrue(report.contains("Prescription count: 1"));
        assertTrue(report.contains("Line 2:"));
        assertTrue(report.contains("Line 3:"));
        assertTrue(report.contains("Line 4:"));
    }

    @Test
    void commandLineWritesTheSameUtf8ReportItPrints() throws IOException {
        Path input = temporaryDirectory.resolve("input.csv");
        Path output = temporaryDirectory.resolve("nested").resolve("report.txt");
        Files.writeString(input, "Aspirin;Pills;1,5;2;false\n", StandardCharsets.UTF_8);

        PrintStream originalOutput = System.out;
        ByteArrayOutputStream printed = new ByteArrayOutputStream();
        System.setOut(new PrintStream(printed, true, StandardCharsets.UTF_8));
        try {
            Lab01Application.main(new String[] {
                    "--input", input.toString(), "--output", output.toString()
            });
        } finally {
            System.setOut(originalOutput);
        }

        assertEquals(printed.toString(StandardCharsets.UTF_8),
                Files.readString(output, StandardCharsets.UTF_8));
        assertTrue(printed.toString(StandardCharsets.UTF_8).contains("Line 1:"));
    }

    @Test
    void mixedInvalidValuesNeverAffectStatistics() {
        String report = Lab01Application.buildReport(List.of(
                "Good;Pills;8;4;true",
                "Too;few",
                "Too;many;1;2;false;extra",
                ";Pills;1;2;false",
                "Bad;Liquid;-3;2;false",
                "Bad;Liquid;NaN;2;false",
                "Bad;Liquid;1;-2;false",
                "Bad;Liquid;1;2;maybe",
                "Second;Liquid;2;0;false"));

        assertTrue(report.contains("Average price: 5.00"));
        assertTrue(report.contains("Prescription count: 1"));
        assertTrue(report.contains("Shortest expiration period: 0"));
        assertTrue(report.contains("Total Rows: 2"));
        assertEquals(7, report.lines().filter(line -> line.startsWith("Line ")).count());
        for (int line = 2; line <= 8; line++) {
            assertTrue(report.contains("Line " + line + ":"), "Missing error for line " + line);
        }
    }

    @Test
    void rejectsNonFinitePrices() {
        assertThrows(IllegalArgumentException.class,
                () -> Lab01Application.parseRow("Aspirin;Pills;NaN;2;false"));
        assertThrows(IllegalArgumentException.class,
                () -> Lab01Application.parseRow("Aspirin;Pills;Infinity;2;false"));
    }
}
