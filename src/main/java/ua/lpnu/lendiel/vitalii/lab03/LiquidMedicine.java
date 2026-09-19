package ua.lpnu.lendiel.vitalii.lab03;

/**
 * Medicine in liquid form.
 */
public final class LiquidMedicine extends Medicine {

    /**
     * Creates a liquid medicine.
     *
     * @param name medicine name
     * @param price medicine price
     * @param expirationDays number of days until expiration
     * @param prescription whether a prescription is required
     */
    public LiquidMedicine(String name, double price, int expirationDays,
            boolean prescription) {
        super(name, MedicineForm.LIQUID, price, expirationDays, prescription);
    }

    /**
     * Returns the form implemented by this subtype.
     *
     * @return {@link MedicineForm#LIQUID}
     */
    @Override
    public MedicineForm getForm() {
        return storedForm();
    }
}
