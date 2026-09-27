package ua.lpnu.lendiel.vitalii.lab04;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class Lab04CliTest {
    @TempDir
    Path temp;

    @Test
    void inputAndOutputWorkIndependently() throws IOException {
        Path input = temp.resolve("input.csv");
        Path output = temp.resolve("report.txt");
        Files.writeString(input, "First;Pills;8;4;true\nToo;few\n"
                + "Second;Liquid;2;0;false\n", StandardCharsets.UTF_8);

        String fromInput = run("--input", input.toString(), "First");
        assertTrue(fromInput.contains("Total correct rows: 2"));
        assertTrue(fromInput.contains("Row 2:"));
        assertTrue(fromInput.contains("Name lookup result: First"));
        String fromDefaultInput = run("--output", output.toString());
        assertTrue(fromDefaultInput.contains("Total correct rows: 10"));
        assertEquals(fromDefaultInput, Files.readString(output, StandardCharsets.UTF_8));

        String combined = run("--input", input.toString(), "--output", output.toString());
        assertTrue(combined.contains("Total correct rows: 2"));
        assertEquals(combined, Files.readString(output, StandardCharsets.UTF_8));
        assertTrue(run("--help").contains("--input PATH"));
    }

    private static String run(String... args) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Lab04Application.main(args);
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
