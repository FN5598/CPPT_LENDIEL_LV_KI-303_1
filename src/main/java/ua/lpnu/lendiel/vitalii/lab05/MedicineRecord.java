package ua.lpnu.lendiel.vitalii.lab05;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ua.lpnu.lendiel.vitalii.lab03.MedicineForm;

/**
 * Flat, immutable CSV representation of a medicine.
 *
 * <p>The reflection exporter sorts these fields by Java name, so imported rows
 * use the order {@code expiresOn, form, name, prescription, price}.</p>
 */
public final class MedicineRecord {
    @CsvColumn("назва")
    private final String name;

    @CsvColumn("форма")
    private final MedicineForm form;

    @CsvColumn("ціна")
    private final double price;

    @CsvColumn("придатність")
    private final LocalDate expiresOn;

    @CsvColumn("рецепт")
    private final boolean prescription;

    /**
     * Creates a validated flat medicine record.
     *
     * @param name medicine name
     * @param form medicine form
     * @param price non-negative finite price
     * @param expiresOn expiration date
     * @param prescription whether a prescription is required
     */
    public MedicineRecord(String name, MedicineForm form, double price,
            LocalDate expiresOn, boolean prescription) {
        this.name = Objects.requireNonNull(name, "Medicine name cannot be null");
        this.form = Objects.requireNonNull(form, "Medicine form cannot be null");
        this.expiresOn = Objects.requireNonNull(expiresOn, "Expiration date cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Medicine name cannot be blank");
        }
        if (!Double.isFinite(price) || price < 0) {
            throw new IllegalArgumentException("Medicine price must be finite and non-negative");
        }
        this.price = price;
        this.prescription = prescription;
    }

    /**
     * Parses fields in the deterministic reflection-export order.
     *
     * @param fields {@code expiresOn, form, name, prescription, price}
     * @return parsed record
     * @throws DataStorageException if the number or value of fields is invalid
     */
    public static MedicineRecord fromFields(List<String> fields)
            throws DataStorageException {
        Objects.requireNonNull(fields, "Medicine record fields cannot be null");
        if (fields.size() != 5) {
            throw new DataStorageException("Medicine record must contain exactly 5 fields");
        }

        try {
            LocalDate expiresOn = LocalDate.parse(fields.get(0).trim());
            MedicineForm form = MedicineForm.fromCsvValue(fields.get(1));
            String name = fields.get(2);
            boolean prescription = parseBoolean(fields.get(3));
            double price = Double.parseDouble(fields.get(4).trim());
            return new MedicineRecord(name, form, price, expiresOn, prescription);
        } catch (DateTimeParseException | NumberFormatException exception) {
            throw new DataStorageException("Invalid MedicineRecord CSV value", exception);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException("Invalid MedicineRecord CSV value", exception);
        }
    }

    private static boolean parseBoolean(String value) {
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!normalized.equals("true") && !normalized.equals("false")) {
            throw new IllegalArgumentException("Invalid prescription value");
        }
        return Boolean.parseBoolean(normalized);
    }

    /**
     * Returns the medicine name.
     *
     * @return medicine name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the medicine form.
     *
     * @return medicine form
     */
    public MedicineForm getForm() {
        return form;
    }

    /**
     * Returns the medicine price.
     *
     * @return medicine price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the expiration date.
     *
     * @return expiration date
     */
    public LocalDate getExpiresOn() {
        return expiresOn;
    }

    /**
     * Indicates whether a prescription is required.
     *
     * @return {@code true} when a prescription is required
     */
    public boolean isPrescription() {
        return prescription;
    }

    /**
     * Indicates whether a prescription is required using domain terminology.
     *
     * @return {@code true} when a prescription is required
     */
    public boolean requiresPrescription() {
        return prescription;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MedicineRecord record)) {
            return false;
        }
        return Double.compare(price, record.price) == 0
                && prescription == record.prescription
                && name.equals(record.name)
                && form == record.form
                && expiresOn.equals(record.expiresOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, form, price, expiresOn, prescription);
    }

    @Override
    public String toString() {
        return "MedicineRecord{name='" + name + "', form=" + form
                + ", price=" + price + ", expiresOn=" + expiresOn
                + ", prescription=" + prescription + '}';
    }
}
