package ua.lpnu.lendiel.vitalii.lab04;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.util.List;

import org.junit.jupiter.api.Test;

import ua.lpnu.lendiel.vitalii.lab03.LiquidMedicine;

class Lab04ApplicationTest {

    @Test
    void loadsTheCsvDataUsedByLab04() throws IOException {
        String[] data = Lab04Application.getData("/lab01/Data.csv");

        assertEquals(10, data.length);
        assertArrayEquals(new String[] {
            "Indian;Pills;5.2;2;false",
            "Pakistani;Pills;3.4;1;true",
            "Pantheon;Liquid;3.2;5;false",
            "Infinity;Liquid;3.4;2;true",
            "Doubledown;Pills;3;12;true",
            "Decrease;Liquid;4.1;30;false",
            "Beyond;Pills;6.2;31;true",
            "Alpha;Liquid;4.8;5;false",
            "Beta;Liquid;5.1;5;true",
            "Indian;Liquid;6.0;20;true"
        }, data);
        assertThrows(NoSuchFileException.class,
                () -> Lab04Application.getData("/missing.csv"));
    }

    @Test
    void parseResultDistinguishesValidAndInvalidRows() {
        Lab04Application.ParseResult valid = new Lab04Application.ParseResult(
                1, new LiquidMedicine("Vitamin", 5, 7, false), null);
        Lab04Application.ParseResult invalid = new Lab04Application.ParseResult(
                2, null, "Invalid medicine row");

        assertTrue(valid.isValid());
        assertFalse(invalid.isValid());
        assertEquals(2, invalid.rowNumber());
        assertEquals("Invalid medicine row", invalid.error());
    }

    @Test
    void parsingPipelineKeepsValidRowsAndCollectsInvalidRows() {
        List<Lab04Application.ParseResult> results = Lab04Application.parseLines(new String[] {
            "Valid;Liquid;5;7;false",
            "Invalid;Capsule;5;7;false"
        });

        assertEquals(2, results.size());
        assertTrue(results.get(0).isValid());
        assertFalse(results.get(1).isValid());
        assertEquals(2, results.get(1).rowNumber());
        assertTrue(results.get(1).error().contains("Medicine can only be"));
        assertEquals(1, results.stream()
                .filter(Lab04Application.ParseResult::isValid)
                .count());
    }

    @Test
    void printsStreamCalculationsAndFormattedLists() {
        String output = runApplication();

        assertTrue(output.contains("Average medicine price: 4.44"));
        assertTrue(output.contains("Shortest medicine expiration period: 1"));
        assertTrue(output.contains("Total medicines that had prescription: 6"));
        assertTrue(output.contains("Total correct rows: 10"));
        assertTrue(output.contains("Errors: 0"));
        assertTrue(output.contains("Medicines that expire within 30 days: 9"));
        assertTrue(output.contains(
                "Names of all medicines: {Indian, Pakistani, Pantheon, Infinity, "
                        + "Doubledown, Decrease, Beyond, Alpha, Beta, Indian}"));
        assertTrue(output.contains(
                "Top Five Medicine names with least expiration time: "
                        + "{Pakistani, Indian, Infinity, Alpha, Beta}"));
        assertTrue(output.contains("PILLS -> 4"));
        assertTrue(output.contains("LIQUID -> 6"));
        assertTrue(output.indexOf("PILLS -> 4") < output.indexOf("LIQUID -> 6"));
    }

    @Test
    void printsTheRequestedMedicineNameWhenProvided() {
        String output = runApplication("Indian");

        assertTrue(output.contains("Name lookup result: Indian"));
    }

    @Test
    void formatsListsWithoutInterpretingPercentSigns() {
        assertEquals("{50% off, Aspirin}",
                Lab04Application.formatList(List.of("50% off", "Aspirin")));
    }

    private static String runApplication(String... args) {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            Lab04Application.main(args);
        } finally {
            System.setOut(originalOutput);
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}
