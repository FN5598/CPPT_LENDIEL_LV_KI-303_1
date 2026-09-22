package ua.lpnu.lendiel.vitalii.lab05;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Gives a CSV column name to a serializable field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvColumn {
    /**
     * Returns the column name written to the CSV header.
     *
     * @return column name
     */
    String value();
}
