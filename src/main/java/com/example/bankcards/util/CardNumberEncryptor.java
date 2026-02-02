    package com.example.bankcards.util;

    import jakarta.persistence.AttributeConverter;
    import jakarta.persistence.Converter;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;

    import javax.crypto.Cipher;
    import javax.crypto.SecretKey;
    import javax.crypto.spec.SecretKeySpec;
    import java.util.Base64;

    @Converter
    @Service
    public class CardNumberEncryptor implements AttributeConverter<String, String> {

        private static final String ALGO = "AES";

        private final SecretKey key;

        public CardNumberEncryptor(@Value("${crypto.aes-key}") String secret) {
            this.key = new SecretKeySpec(secret.getBytes(), ALGO);
        }

        @Override
        public String convertToDatabaseColumn(String value) {
            if (value == null) return null;
            try {
                Cipher cipher = Cipher.getInstance(ALGO);
                cipher.init(Cipher.ENCRYPT_MODE, key);
                return Base64.getEncoder().encodeToString(
                        cipher.doFinal(value.getBytes())
                );
            } catch (Exception e) {
                throw new IllegalStateException("Encrypt error", e);
            }
        }

        @Override
        public String convertToEntityAttribute(String dbValue) {
            if (dbValue == null) return null;
            try {
                Cipher cipher = Cipher.getInstance(ALGO);
                cipher.init(Cipher.DECRYPT_MODE, key);
                return new String(cipher.doFinal(
                        Base64.getDecoder().decode(dbValue)
                ));
            } catch (Exception e) {
                throw new IllegalStateException("Decrypt error", e);
            }
        }
    }
