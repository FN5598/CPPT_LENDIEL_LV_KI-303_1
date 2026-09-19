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
    void parsesTheExistingCsvFormatIntoConcreteSubtypes() {
        Medicine prescription = MedicineFactory.fromCsv("Aspirin;Pills;12.50;30;TRUE");
        Medicine freeSale = MedicineFactory.fromCsv("Vitamin;Liquid;5;7;false");

        assertTrue(prescription instanceof PrescriptionMedicine);
        assertTrue(freeSale instanceof FreeMedicine);
        assertEquals(MedicineKind.PRESCRIPTION, prescription.getKind());
        assertEquals(MedicineKind.FREE_SALE, freeSale.getKind());
        assertTrue(prescription.getIsPrescription());
        assertFalse(freeSale.getIsPrescription());
        assertEquals("Aspirin;Pills;12.50;30;true", prescription.toString());
        assertEquals("Vitamin;Liquid;5.00;7;false", freeSale.toString());
    }

    @Test
    void validatesSharedFieldsInTheBaseType() {
        assertThrows(NullPointerException.class,
                () -> new FreeMedicine(null, "Pills", 5, 7));
        assertThrows(IllegalArgumentException.class,
                () -> new FreeMedicine("", "Pills", 5, 7));
        assertThrows(IllegalArgumentException.class,
                () -> new FreeMedicine("Medicine", "Capsule", 5, 7));
        assertThrows(IllegalArgumentException.class,
                () -> new FreeMedicine("Medicine", "Pills", -1, 7));
        assertThrows(IllegalArgumentException.class,
                () -> new FreeMedicine("Medicine", "Pills", 5, -1));
    }

    @Test
    void parsesAndValidatesMedicineKind() {
        assertEquals(MedicineKind.PRESCRIPTION, MedicineKind.fromCsvValue(" TRUE "));
        assertEquals(MedicineKind.FREE_SALE, MedicineKind.fromCsvValue("false"));
        assertTrue(MedicineKind.PRESCRIPTION.requiresPrescription());
        assertFalse(MedicineKind.FREE_SALE.requiresPrescription());
        assertThrows(IllegalArgumentException.class,
                () -> MedicineKind.fromCsvValue("unknown"));
    }

    @Test
    void evaluatesAvailabilityPolymorphicallyThroughTheCommonType() {
        List<Medicine> medicines = List.of(
                new PrescriptionMedicine("Prescription", "Pills", 10, 3),
                new FreeMedicine("Free sale", "Liquid", 5, 3),
                new FreeMedicine("Expired", "Pills", 5, 0));

        assertTrue(medicines.get(0).isAvailable(true));
        assertFalse(medicines.get(0).isAvailable(false));
        assertTrue(medicines.get(1).isAvailable(false));
        assertFalse(medicines.get(2).isAvailable(true));
    }

    @Test
    void equalsAndHashCodeWorkInHashSet() {
        Medicine first = new FreeMedicine("Vitamin", "Liquid", 5, 7);
        Medicine same = new FreeMedicine("Vitamin", "Liquid", 5, 7);
        Medicine different = new FreeMedicine("Vitamin", "Liquid", 6, 7);

        Set<Medicine> medicines = new HashSet<>(List.of(first, same, different));

        assertEquals(first, same);
        assertEquals(first.hashCode(), same.hashCode());
        assertNotEquals(first, different);
        assertEquals(2, medicines.size());
    }

    @Test
    void rejectsMalformedRowsWithoutChangingTheInputContract() {
        assertThrows(NullPointerException.class, () -> MedicineFactory.fromCsv(null));
        assertThrows(IllegalArgumentException.class,
                () -> MedicineFactory.fromCsv("Medicine;Pills;5;7"));
        assertThrows(IllegalArgumentException.class,
                () -> MedicineFactory.fromCsv("Medicine;Pills;cost;7;false"));
        assertThrows(IllegalArgumentException.class,
                () -> MedicineFactory.fromCsv("Medicine;Pills;5;7;unknown"));
    }

    @Test
    void loadsTheSharedClasspathData() throws IOException {
        String[] data = Lab03Application.getData("/lab01/Data.csv");

        assertEquals(6, data.length);
        assertArrayEquals(new String[] {
                "Indian;Pills;5.2;2;false",
                "Pakistani;Pills;3.4;1;true",
                "Pantheon;Liquid;3.2;5;false",
                "Infinity;Liquid;3.4;2;true",
                "Doubledown;Pills;3;12;true",
                "Decrease;Liquid;  ;   ;  "
        }, data);
        assertThrows(NoSuchFileException.class,
                () -> Lab03Application.getData("/lab03/missing.csv"));
    }

    @Test
    void printsTheLab02CompatibleReport() throws Exception {
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win")
                        ? "java.exe" : "java");
        Process process = new ProcessBuilder(
                javaExecutable.toString(),
                "-cp",
                System.getProperty("java.class.path"),
                Lab03Application.class.getName())
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(0, process.waitFor());
        assertTrue(output.contains("Average medicine price: 3.64"));
        assertTrue(output.contains("Shortest medicine expiration period: 1"));
        assertTrue(output.contains("Total medicines that had prescription: 3"));
        assertTrue(output.contains("Total correct rows: 5"));
        assertTrue(output.contains("Errors: 1"));
    }
}
