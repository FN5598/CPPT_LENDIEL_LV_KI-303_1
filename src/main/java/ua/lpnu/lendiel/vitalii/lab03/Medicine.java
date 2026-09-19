package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Locale;
import java.util.Objects;

/**
 * Common immutable data and behavior for all medicines.
 */
public abstract class Medicine {
    private final String name;
    private final MedicineForm form;
    private final double price;
    private final int expirationDays;
    private final boolean prescription;

    /**
     * Creates a validated medicine.
     *
     * @param name medicine name
     * @param form medicine form
     * @param price medicine price; it must be finite and non-negative
     * @param expirationDays number of days until expiration; it must be
     *                       non-negative
     * @param prescription whether a prescription is required
     * @throws NullPointerException if {@code name} or {@code form} is null
     * @throws IllegalArgumentException if the name is blank or a numeric value
     *                                  is invalid
     */
    protected Medicine(String name, MedicineForm form, double price, int expirationDays,
            boolean prescription) {
        this(validateFields(name, form, price, expirationDays, prescription));
    }

    private Medicine(ValidatedFields fields) {
        this.name = fields.name();
        this.form = fields.form();
        this.price = fields.price();
        this.expirationDays = fields.expirationDays();
        this.prescription = fields.prescription();
    }

    private static ValidatedFields validateFields(String name, MedicineForm form,
            double price, int expirationDays, boolean prescription) {
        String validatedName = Objects.requireNonNull(name, "Name cannot be null");
        MedicineForm validatedForm = Objects.requireNonNull(form, "Medicine form cannot be null");
        if (validatedName.isBlank()) {
            throw new IllegalArgumentException("Medicine name cannot be blank.");
        }
        if (!Double.isFinite(price) || price < 0 || expirationDays < 0) {
            throw new IllegalArgumentException(
                    "Medicine price or expiration days cannot be negative.");
        }
        return new ValidatedFields(validatedName, validatedForm, price, expirationDays,
                prescription);
    }

    private record ValidatedFields(String name, MedicineForm form, double price,
            int expirationDays, boolean prescription) {
    }

    /**
     * Returns the physical form stored in this medicine.
     *
     * @return medicine form
     */
    public abstract MedicineForm getForm();

    /**
     * Returns the validated form stored by the base class.
     *
     * @return stored medicine form
     */
    protected final MedicineForm storedForm() {
        return form;
    }

    /**
     * Evaluates whether the medicine can be dispensed in the given situation.
     *
     * @param prescriptionProvided whether the customer has a valid prescription
     * @return {@code true} when the medicine can be dispensed
     */
    public final boolean isAvailable(boolean prescriptionProvided) {
        return !isExpired() && (!prescription || prescriptionProvided);
    }

    /**
     * Returns the medicine name.
     *
     * @return medicine name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the medicine price.
     *
     * @return medicine price
     */
    public final double getPrice() {
        return price;
    }

    /**
     * Returns the number of days until expiration.
     *
     * @return expiration period in days
     */
    public final int getExpirationDays() {
        return expirationDays;
    }

    /**
     * Indicates whether this medicine requires a prescription.
     *
     * @return {@code true} for prescription medicines
     */
    public final boolean requiresPrescription() {
        return prescription;
    }

    /**
     * Returns whether the medicine has expired.
     *
     * @return {@code true} when no valid days remain
     */
    protected final boolean isExpired() {
        return expirationDays == 0;
    }

    /**
     * Converts the medicine to the CSV representation used by the application.
     *
     * @return semicolon-separated medicine data
     */
    public final String toCsvRow() {
        return String.format(Locale.ROOT, "%s;%s;%.2f;%d;%s",
                name, form.toCsvValue(), price, expirationDays,
                requiresPrescription());
    }

    /**
     * Returns the CSV representation of this medicine.
     *
     * @return semicolon-separated medicine data
     */
    @Override
    public final String toString() {
        return toCsvRow();
    }

    /**
     * Compares medicines by concrete type and common data.
     *
     * @param other object to compare with
     * @return {@code true} when both objects represent the same medicine
     */
    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Medicine medicine = (Medicine) other;
        return Double.compare(price, medicine.price) == 0
                && expirationDays == medicine.expirationDays
                && name.equals(medicine.name)
                && form == medicine.form
                && prescription == medicine.prescription;
    }

    /**
     * Returns a hash based on the same fields used by {@link #equals(Object)}.
     *
     * @return medicine hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), name, form, price, expirationDays, prescription);
    }
}
