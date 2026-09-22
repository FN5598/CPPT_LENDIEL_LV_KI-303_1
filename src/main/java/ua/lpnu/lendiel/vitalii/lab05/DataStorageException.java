package ua.lpnu.lendiel.vitalii.lab05;

/**
 * Checked exception for file, CSV, conversion, and reflection failures.
 */
public class DataStorageException extends Exception {
    /**
     * Creates a storage exception with a message.
     *
     * @param message explanation of the failure
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Creates a storage exception while preserving the original cause.
     *
     * @param message explanation of the failure
     * @param cause original failure
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
