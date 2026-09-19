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
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import ua.lpnu.lendiel.vitalii.lab03.Lab03Application;
import ua.lpnu.lendiel.vitalii.lab03.LiquidMedicine;
import ua.lpnu.lendiel.vitalii.lab03.Medicine;
import ua.lpnu.lendiel.vitalii.lab03.MedicineFactory;
import ua.lpnu.lendiel.vitalii.lab03.MedicineForm;

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
    void namedQueriesReturnExpectedResults() throws IOException {
        List<Medicine> medicines = loadMedicines();

        assertEquals(9, Lab04Application.countExpiringWithinThirtyDays(medicines));
        assertEquals(List.of("Indian", "Pakistani", "Pantheon", "Infinity",
                        "Doubledown", "Decrease", "Beyond", "Alpha", "Beta", "Indian"),
                Lab04Application.mapMedicineNames(medicines));

        Map<MedicineForm, Long> countByForm = Lab04Application.countByForm(medicines);
        assertEquals(4L, countByForm.get(MedicineForm.PILLS));
        assertEquals(6L, countByForm.get(MedicineForm.LIQUID));

        DoubleSummaryStatistics statistics = Lab04Application.summarizePrices(medicines);
        assertEquals(10, statistics.getCount());
        assertEquals(44.4, statistics.getSum(), 0.0001);
        assertEquals(List.of("Pakistani", "Indian", "Infinity", "Alpha", "Beta"),
                Lab04Application.topFiveByExpiration(medicines));
    }

    @Test
    void namedQueriesAndSearchHandleEmptyInput() {
        List<Medicine> empty = List.of();

        assertEquals(0, Lab04Application.countExpiringWithinThirtyDays(empty));
        assertTrue(Lab04Application.mapMedicineNames(empty).isEmpty());
        assertTrue(Lab04Application.countByForm(empty).isEmpty());
        assertEquals(0, Lab04Application.summarizePrices(empty).getCount());
        assertTrue(Lab04Application.topFiveByExpiration(empty).isEmpty());
        assertEquals(-1, Lab04Application.shortestExpirationPeriod(empty));
        assertEquals(0, Lab04Application.countPrescriptionMedicines(empty));
        assertTrue(Lab04Application.findByName(empty, "Unknown").isEmpty());
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
                "Price statistics: count=10, sum=44.40, average=4.44, min=3.00, max=6.20"));
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
    void omitsLookupOutputWhenMedicineIsNotFound() {
        String output = runApplication("Unknown");

        assertFalse(output.contains("Name lookup result:"));
    }

    @Test
    void streamSummaryMatchesTheLab03LoopSummary() {
        String loopOutput = runApplication(() -> Lab03Application.main(new String[0]));
        String streamOutput = runApplication(() -> Lab04Application.main(new String[0]));

        for (String prefix : List.of(
                "Average medicine price:",
                "Shortest medicine expiration period:",
                "Total medicines that had prescription:",
                "Total correct rows:",
                "Errors:")) {
            assertEquals(lineWithPrefix(loopOutput, prefix), lineWithPrefix(streamOutput, prefix));
        }
    }

    @Test
    void formatsListsWithoutInterpretingPercentSigns() {
        assertEquals("{50% off, Aspirin}",
                Lab04Application.formatList(List.of("50% off", "Aspirin")));
    }

    private static String runApplication(String... args) {
        return runApplication(() -> Lab04Application.main(args));
    }

    private static String runApplication(Runnable application) {
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        try {
            application.run();
        } finally {
            System.setOut(originalOutput);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    private static String lineWithPrefix(String output, String prefix) {
        return output.lines()
                .filter(line -> line.startsWith(prefix))
                .findFirst()
                .orElseThrow();
    }

    private static List<Medicine> loadMedicines() throws IOException {
        return Arrays.stream(Lab04Application.getData("/lab01/Data.csv"))
                .map(MedicineFactory::fromCsv)
                .toList();
    }
}
