package ua.lpnu.lendiel.vitalii.lab03;

import java.util.Locale;
import java.util.Objects;

/**
 * Common base type for medicines handled by laboratory work 3.
 *
 * <p>The class owns fields and validation rules shared by every medicine. The
 * availability decision is intentionally delegated to concrete subclasses so
 * clients can work with a collection of {@code Medicine} objects without
 * knowing their concrete types.</p>
 */
public abstract class Medicine {
    private final String name;
    private final String form;
    private final double price;
    private final int daysToExpire;
    private final MedicineKind kind;
    private final boolean prescription;

    /**
     * Creates a validated medicine.
     *
     * @param name medicine name
     * @param form medicine form; only {@code Liquid} and {@code Pills} are
     *             accepted, ignoring case
     * @param kind dispensing category
     * @param price medicine price; it must not be negative
     * @param daysToExpire number of days until expiration; it must not be
     *                     negative
     * @param prescription whether a prescription is required
     * @throws NullPointerException if a required reference is {@code null}
     * @throws IllegalArgumentException if the name is blank or a numeric value
     *                                  is invalid
     */
    protected Medicine(String name, String form, double price, int daysToExpire,
            MedicineKind kind, boolean prescription) {
        this(validateFields(name, form, price, daysToExpire, kind, prescription));
    }

    private Medicine(ValidatedFields fields) {
        this.name = fields.name();
        this.form = fields.form();
        this.price = fields.price();
        this.daysToExpire = fields.daysToExpire();
        this.kind = fields.kind();
        this.prescription = fields.prescription();
    }

    private static ValidatedFields validateFields(String name, String form, double price,
            int daysToExpire, MedicineKind kind, boolean prescription) {
        String validatedName = Objects.requireNonNull(name, "Name cannot be null");
        String validatedForm = Objects.requireNonNull(form, "Form cannot be null");
        MedicineKind validatedKind = Objects.requireNonNull(kind, "Medicine kind cannot be null");

        if (validatedName.isBlank()) {
            throw new IllegalArgumentException("Medicine name cannot be blank.");
        }
        if (!validatedForm.equalsIgnoreCase("Liquid") && !validatedForm.equalsIgnoreCase("Pills")) {
            throw new IllegalArgumentException(
                    "Medicine can only be in 2 forms \"Liquid\" or \"Pills\".");
        }
        if (validatedKind.requiresPrescription() != prescription) {
            throw new IllegalArgumentException("Medicine kind and prescription value must match.");
        }
        if (!Double.isFinite(price) || price < 0 || daysToExpire < 0) {
            throw new IllegalArgumentException(
                    "Medicine price or days until expiration cannot be negative.");
        }
        return new ValidatedFields(validatedName, validatedForm, price, daysToExpire,
                validatedKind, prescription);
    }

    private record ValidatedFields(String name, String form, double price,
            int daysToExpire, MedicineKind kind, boolean prescription) {
    }

    /**
     * Evaluates whether the medicine can be dispensed in the given situation.
     *
     * @param prescriptionProvided whether the customer has a valid
     *                              prescription
     * @return {@code true} when the medicine can be dispensed
     */
    public abstract boolean isAvailable(boolean prescriptionProvided);

    /**
     * Returns the medicine name.
     *
     * @return medicine name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the medicine form.
     *
     * @return medicine form
     */
    public final String getForm() {
        return form;
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
    public final int getDaysToExpire() {
        return daysToExpire;
    }

    /**
     * Indicates whether the medicine requires a prescription.
     *
     * @return {@code true} for prescription medicines
     */
    public final boolean getIsPrescription() {
        return prescription;
    }

    /**
     * Returns the dispensing category.
     *
     * @return medicine kind
     */
    public final MedicineKind getKind() {
        return kind;
    }

    /**
     * Indicates whether the medicine requires a prescription.
     *
     * @return {@code true} for prescription medicines
     */
    public final boolean requiresPrescription() {
        return prescription;
    }

    /**
     * Returns whether the medicine has expired under the model used by this
     * laboratory.
     *
     * @return {@code true} when no valid days remain
     */
    protected final boolean isExpired() {
        return daysToExpire == 0;
    }

    /**
     * Converts the medicine to the CSV representation used in Lab 02.
     *
     * @return semicolon-separated medicine data
     */
    public final String toCsvRow() {
        return String.format(Locale.ROOT, "%s;%s;%.2f;%d;%s",
                name, form, price, daysToExpire,
                requiresPrescription());
    }

    /**
     * Returns the compatible CSV representation.
     *
     * @return semicolon-separated medicine data
     */
    @Override
    public final String toString() {
        return toCsvRow();
    }

    /**
     * Compares medicines by concrete type and all shared identity fields.
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
                && daysToExpire == medicine.daysToExpire
                && name.equals(medicine.name)
                && prescription == medicine.prescription
                && form.equals(medicine.form)
                && kind == medicine.kind;
    }

    /**
     * Returns a hash based on the same fields used by {@link #equals(Object)}.
     *
     * @return medicine hash code
     */
    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), name, form, price, daysToExpire, kind, prescription);
    }
}
