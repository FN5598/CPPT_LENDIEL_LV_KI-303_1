package ua.lpnu.lendiel.vitalii.lab05;

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

class Lab05CliTest {
    @TempDir
    Path temp;

    @Test
    void inputAndOutputWorkIndependently() throws Exception {
        Path input = temp.resolve("input.csv");
        Path output = temp.resolve("export.csv");
        Files.writeString(input, "First;Pills;8;4;true\nToo;few\n"
                + "Second;Liquid;2;0;false\n", StandardCharsets.UTF_8);

        String fromInput = run("--input", input.toString(), "--output", output.toString());
        assertTrue(fromInput.contains("Repository records: 2"));
        assertTrue(fromInput.contains("Round-trip equal: true"));
        assertEquals(2, CsvImporter.read(output, MedicineRecord.class,
                MedicineRecord::fromFields).size());

        String fromInputOnly = run("--input", input.toString());
        assertTrue(fromInputOnly.contains("Repository records: 2"));
        assertEquals(2, CsvImporter.read(Path.of("target", "lab05", "medicines.csv"),
                MedicineRecord.class, MedicineRecord::fromFields).size());

        String fromDefaultInput = run("--output", output.toString());
        assertTrue(fromDefaultInput.contains("Repository records: 10"));
        assertEquals(10, CsvImporter.read(output, MedicineRecord.class,
                MedicineRecord::fromFields).size());

        String positionalOutput = run(output.toString());
        assertTrue(positionalOutput.contains("Repository records: 10"));
        assertTrue(run("--help").contains("--input PATH"));
    }

    private static String run(String... args) throws IOException {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            Lab05Application.main(args);
        } finally {
            System.setOut(original);
        }
        return output.toString(StandardCharsets.UTF_8);
    }
}
