/*
 * @ {#} DateValidator.java   1.0     24/09/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package fit.patient_service.validators;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/*
 * @description: Validator for date fields
 * @author: Tran Hien Vinh
 * @date:   24/09/2025
 * @version:    1.0
 */
@Component
public class DateValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Validate date of birth
    public void validateDateOfBirth(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new IllegalArgumentException("Date of birth cannot be null or empty");
        }

        if (!isValidDate(dateString)) {
            throw new IllegalArgumentException("Invalid date format: " + dateString + ". Expected format: yyyy-MM-dd");
        }

        if (!isDateInPast(dateString)) {
            throw new IllegalArgumentException("Date of birth must be in the past");
        }
    }

    // Check if the date string is valid
    public boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Check if the date is in the past
    public boolean isDateInPast(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
