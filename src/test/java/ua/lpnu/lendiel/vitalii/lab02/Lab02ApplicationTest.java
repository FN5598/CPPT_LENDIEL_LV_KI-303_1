package ua.lpnu.lendiel.vitalii.lab02;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class Lab02ApplicationTest {

    private static final String MEDICINE_CLASS_NAME = Lab02Application.class.getName() + "$MedicineInformation";
    private static final String SUMMARY_CLASS_NAME = Lab02Application.class.getName()
            + "$MedicineInformationSummary";

    @Test
    void parsesValidCsvRowsAndTrimsTextFields() {
        Object medicine = parseMedicine("  Aspirin ; pills ; 12.50 ; 30 ; TRUE");

        assertAll(
                () -> assertEquals(12.50, invoke(medicine, "getPrice")),
                () -> assertEquals(30, invoke(medicine, "getDaysToExpire")),
                () -> assertEquals(true, invoke(medicine, "getIsPrescription")),
                () -> assertEquals("Aspirin;pills;12.50;30;true", medicine.toString()));
    }

    @Test
    void acceptsCaseInsensitiveMedicineFormsAndBooleanValues() {
        Object medicine = parseMedicine("Medicine;LIQUID;5;7;fAlSe");

        assertEquals(false, invoke(medicine, "getIsPrescription"));
        assertEquals("Medicine;LIQUID;5.00;7;false", medicine.toString());
    }

    @Test
    void rejectsNullAndMalformedCsvRows() {
        assertThrows(NullPointerException.class, () -> parseMedicine(null));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;5;7"));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;5;7;false;extra"));
    }

    @Test
    void rejectsInvalidNumericAndBooleanValues() {
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;cost;7;false"));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;5;days;false"));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;5;7;unknown"));
    }

    @Test
    void rejectsUnsupportedFormsAndNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Capsule;5;7;false"));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;-1;7;false"));
        assertThrows(IllegalArgumentException.class, () -> parseMedicine("Medicine;Pills;5;-1;false"));
    }

    @Test
    void validatesSummaryValues() {
        assertNotNull(createSummary(0, Integer.MAX_VALUE, 0));
        assertThrows(IllegalArgumentException.class, () -> createSummary(-1, 1, 0));
        assertThrows(IllegalArgumentException.class, () -> createSummary(1, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> createSummary(1, 1, -1));
        assertThrows(IllegalArgumentException.class, () -> createSummary(Double.NaN, 1, 0));
    }

    @Test
    void loadsCsvDataFromClasspath() throws IOException {
        String[] data = loadData("/lab01/Data.csv");

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
        assertThrows(NoSuchFileException.class, () -> loadData("/lab02/missing.csv"));
    }

    @Test
    void mainPrintsStatisticsAndInvalidRowErrors() throws Exception {
        Path javaExecutable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java");
        Process process = new ProcessBuilder(
                javaExecutable.toString(),
                "-cp",
                System.getProperty("java.class.path"),
                Lab02Application.class.getName())
                .redirectErrorStream(true)
                .start();

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(0, process.waitFor());
        assertAll(
                () -> assertTrue(output.contains("Average medicine price: 4.44")),
                () -> assertTrue(output.contains("Shortest medicine expiration period: 1")),
                () -> assertTrue(output.contains("Total medicines that had prescription: 6")),
                () -> assertTrue(output.contains("Total correct rows: 10")),
                () -> assertTrue(output.contains("Errors: 0")));
    }

    private static Object parseMedicine(String line) {
        try {
            Class<?> medicineClass = Class.forName(MEDICINE_CLASS_NAME);
            Method fromCsv = medicineClass.getDeclaredMethod("fromCsv", String.class);
            fromCsv.setAccessible(true);
            return fromCsv.invoke(null, line);
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object createSummary(double averagePrice, int shortestExpirationPeriod, int prescriptionCount) {
        try {
            Class<?> summaryClass = Class.forName(SUMMARY_CLASS_NAME);
            Constructor<?> constructor = summaryClass.getDeclaredConstructor(
                    double.class, int.class, int.class);
            constructor.setAccessible(true);
            return constructor.newInstance(averagePrice, shortestExpirationPeriod, prescriptionCount);
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static String[] loadData(String path) throws IOException {
        try {
            Method getData = Lab02Application.class.getDeclaredMethod("getData", String.class);
            getData.setAccessible(true);
            return (String[]) getData.invoke(null, path);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException ioException) {
                throw ioException;
            }
            throw rethrow(cause);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static Object invoke(Object target, String methodName) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(target);
        } catch (InvocationTargetException e) {
            throw rethrow(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private static RuntimeException rethrow(Throwable cause) {
        if (cause instanceof RuntimeException runtimeException) {
            return runtimeException;
        }
        if (cause instanceof Error error) {
            throw error;
        }
        throw new AssertionError(cause);
    }
}
