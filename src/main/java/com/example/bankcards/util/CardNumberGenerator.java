package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private final Random random = new SecureRandom();

    /**
     * Генерирует валидный номер карты с алгоритмом Луна
     */
    public String generateCardNumber() {
        // Первые 6 цифр - BIN (Bank Identification Number)
        String bin = "400000"; // Visa

        // Следующие 9 цифр - случайные
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            accountNumber.append(random.nextInt(10));
        }

        // Собираем номер без контрольной цифры
        String partialCardNumber = bin + accountNumber;

        // Вычисляем контрольную цифру по алгоритму Луна
        int checkDigit = calculateLuhnCheckDigit(partialCardNumber);

        return partialCardNumber + checkDigit;
    }

    /**
     * Алгоритм Луна для вычисления контрольной цифры
     */
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }
}