package ua.lpnu.lendiel.vitalii;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/**
 * Reads release metadata generated from Maven project properties.
 */
public final class VersionInfo {
    private static final String RESOURCE = "/version.properties";
    private static final Properties PROPERTIES = loadProperties();

    private VersionInfo() {
    }

    /**
     * Returns the Maven version of the current release.
     *
     * @return current project version
     */
    public static String projectVersion() {
        return required("project.version");
    }

    /**
     * Returns the release version assigned to a laboratory.
     *
     * @param lab laboratory key such as {@code lab01}
     * @return laboratory version
     */
    public static String labVersion(String lab) {
        String normalized = Objects.requireNonNull(lab, "Lab key cannot be null")
                .trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("lab0[1-5]")) {
            throw new IllegalArgumentException("Unknown laboratory: " + lab);
        }
        return required(normalized + ".version");
    }

    /**
     * Returns the laboratory represented by the current release.
     *
     * @return current release lab key
     */
    public static String releaseLab() {
        return required("release.lab");
    }

    private static String required(String key) {
        String value = PROPERTIES.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing version property: " + key);
        }
        return value;
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = VersionInfo.class.getResourceAsStream(RESOURCE)) {
            if (input == null) {
                throw new IllegalStateException("Missing resource: " + RESOURCE);
            }
            properties.load(input);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + RESOURCE, exception);
        }
    }
}

