package ua.lpnu.lendiel.vitalii.lab01;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class Lab01ApplicationTest {

    private static final Path ERROR_FILE = Path.of("Errors.txt");

    @AfterEach
    void removeGeneratedErrorFile() throws IOException {
        Files.deleteIfExists(ERROR_FILE);
    }

    @Test
    void loadsCsvDataFromResources() throws IOException {
        String[] data = Lab01Application.getData();

        assertEquals(6, data.length);
        assertArrayEquals(new String[] {
            "Name;Form;5.2;2;false",
            "Name;Form;3.4;1;true",
            "Pantheon;Form;3.2;5;false",
            "Name;asdasd asda s;3.4;2;true",
            "Name;asdasd asda s;3;12;true"
        }, java.util.Arrays.copyOf(data, 5));
        assertTrue(data[5].startsWith("Name;asdasd asda s;"));
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
        assertTrue(result.contains("Invalid number format"));
    }
}