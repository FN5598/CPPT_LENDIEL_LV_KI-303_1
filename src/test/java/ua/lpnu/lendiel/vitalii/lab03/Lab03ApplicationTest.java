package ua.lpnu.lendiel.vitalii.lab03;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class Lab03ApplicationTest {

        @Test
        void parsesTheCsvFormatIntoConcreteSubtypes() {
                Medicine pills = MedicineFactory.fromCsv("Aspirin;Pills;12.50;30;TRUE");
                Medicine liquid = MedicineFactory.fromCsv("Vitamin;Liquid;5;7;false");

                assertTrue(pills instanceof PillsMedicine);
                assertTrue(liquid instanceof LiquidMedicine);
                assertEquals(MedicineForm.PILLS, pills.getForm());
                assertEquals(MedicineForm.LIQUID, liquid.getForm());
                assertTrue(pills.requiresPrescription());
                assertFalse(liquid.requiresPrescription());
                assertEquals("Aspirin;Pills;12.50;30;true", pills.toString());
                assertEquals("Vitamin;Liquid;5.00;7;false", liquid.toString());
        }

        @Test
        void validatesSharedFieldsInTheBaseType() {
                assertThrows(NullPointerException.class,
                                () -> new PillsMedicine(null, 5, 7, false));
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineFactory.fromCsv("Medicine; ;5;7;false"));
                assertThrows(IllegalArgumentException.class,
                                () -> new PillsMedicine("", 5, 7, false));
                assertThrows(IllegalArgumentException.class,
                                () -> new PillsMedicine("Medicine", -1, 7, false));
                assertThrows(IllegalArgumentException.class,
                                () -> new PillsMedicine("Medicine", 5, -1, false));
                assertThrows(IllegalArgumentException.class,
                                () -> new PillsMedicine("Medicine", Double.NaN, 7, false));
        }

        @Test
        void parsesMedicineFormsFromCsvValues() {
                assertEquals(MedicineForm.PILLS, MedicineForm.fromCsvValue(" PILLS "));
                assertEquals(MedicineForm.LIQUID, MedicineForm.fromCsvValue("liquid"));
                assertEquals("Pills", MedicineForm.PILLS.toCsvValue());
                assertEquals("Liquid", MedicineForm.LIQUID.toCsvValue());
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineForm.fromCsvValue("Capsule"));
        }

        @Test
        void evaluatesAvailabilityUsingTheCommonPrescriptionRule() {
                List<Medicine> medicines = List.of(
                                new PillsMedicine("Prescription", 10, 3, true),
                                new LiquidMedicine("Over the counter", 5, 3, false),
                                new LiquidMedicine("Expired", 5, 0, false));

                assertTrue(medicines.get(0).isAvailable(true));
                assertFalse(medicines.get(0).isAvailable(false));
                assertTrue(medicines.get(1).isAvailable(false));
                assertFalse(medicines.get(2).isAvailable(true));
                assertTrue(medicines.get(0).requiresPrescription());
                assertFalse(medicines.get(1).requiresPrescription());
                assertFalse(medicines.get(2).requiresPrescription());
        }

        @Test
        void equalsAndHashCodeWorkInHashSet() {
                Medicine first = new LiquidMedicine("Vitamin", 5, 7, false);
                Medicine same = new LiquidMedicine("Vitamin", 5, 7, false);
                Medicine different = new PillsMedicine("Vitamin", 5, 7, false);

                Set<Medicine> medicines = new HashSet<>(List.of(first, same, different));

                assertEquals(first, same);
                assertEquals(first.hashCode(), same.hashCode());
                assertNotEquals(first, different);
                assertEquals(2, medicines.size());
        }

        @Test
        void rejectsMalformedRowsBeforeCreatingObjects() {
                assertThrows(NullPointerException.class, () -> MedicineFactory.fromCsv(null));
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineFactory.fromCsv("Medicine;Pills;5;7"));
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineFactory.fromCsv("Medicine;Capsule;5;7;false"));
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineFactory.fromCsv("Medicine;Pills; ;7;false"));
                assertThrows(IllegalArgumentException.class,
                                () -> MedicineFactory.fromCsv("Medicine;Pills;5;7;unknown"));
        }

        @Test
        void loadsTheExpandedClasspathData() throws IOException {
                String[] data = Lab03Application.getData("/lab01/Data.csv");

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
                                () -> Lab03Application.getData("/lab03/missing.csv"));
        }

        @Test
        void printsTheUpdatedReport() throws Exception {
                Path javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                                System.getProperty("os.name").toLowerCase().contains("win")
                                                ? "java.exe"
                                                : "java");
                Process process = new ProcessBuilder(
                                javaExecutable.toString(),
                                "-cp",
                                System.getProperty("java.class.path"),
                                Lab03Application.class.getName())
                                .redirectErrorStream(true)
                                .start();

                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                assertEquals(0, process.waitFor());
                assertTrue(output.contains("Average medicine price: 4.44"));
                assertTrue(output.contains("Shortest medicine expiration period: 1"));
                assertTrue(output.contains("Total medicines that had prescription: 6"));
                assertTrue(output.contains("Total correct rows: 10"));
                assertTrue(output.contains("Errors: 0"));
        }
}
