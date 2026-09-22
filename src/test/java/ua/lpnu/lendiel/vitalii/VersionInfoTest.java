package ua.lpnu.lendiel.vitalii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class VersionInfoTest {
    @Test
    void generatedMetadataMatchesTheActiveRelease() {
        assertTrue(VersionInfo.projectVersion().matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue(VersionInfo.releaseLab().matches("lab0[1-5]"));
        assertEquals(VersionInfo.projectVersion(),
                VersionInfo.labVersion(VersionInfo.releaseLab()));
    }

    @Test
    void activeLabVersionCommandMatchesGeneratedProjectVersion() throws Exception {
        String lab = VersionInfo.releaseLab();
        String suffix = lab.substring(3);
        Class<?> application = Class.forName(
                "ua.lpnu.lendiel.vitalii.lab" + suffix + ".Lab" + suffix + "Application");
        Method main = application.getMethod("main", String[].class);
        PrintStream originalOutput = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
        try {
            main.invoke(null, (Object) new String[] { "--version" });
        } finally {
            System.setOut(originalOutput);
        }
        assertEquals(VersionInfo.projectVersion(),
                output.toString(StandardCharsets.UTF_8).trim());
    }

    @Test
    void rejectsUnknownLaboratoryKeys() {
        assertThrows(IllegalArgumentException.class,
                () -> VersionInfo.labVersion("lab06"));
    }
}

