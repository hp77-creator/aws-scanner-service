package io.hp77creator.github.awsscannerservice.worker.scanner.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class LuhnValidator {

    /**
     * Validates a card number using the Luhn algorithm.
     * 
     * @param cardNumber The card number string (digits only)
     * @return true if the number passes Luhn validation, false otherwise
     */
    public static boolean validate(String cardNumber) {
        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        // Remove any non-digit characters
        String digitsOnly = cardNumber.replaceAll("\\D", "");
        
        if (digitsOnly.length() < 13 || digitsOnly.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = digitsOnly.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(digitsOnly.charAt(i));
            
            if (digit < 0 || digit > 9) {
                return false;
            }

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }
}
