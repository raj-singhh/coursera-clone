    // GenerateSecretKey.java
    // You can delete this file after generating your key.
    package com.example.coursera_clone; // Or any package, as this is temporary

    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.security.Keys;
    import java.util.Base64;

    public class GenerateSecretKey {
        public static void main(String[] args) {
            // Generate a secure random key for HS512 (512-bit key)
            // HS256 (256-bit key) is also common, but HS512 provides more strength.
            // The key generated will be at least 64 bytes (512 bits) long.
            byte[] keyBytes = Keys.secretKeyFor(SignatureAlgorithm.HS512).getEncoded();
            String base64Key = Base64.getEncoder().encodeToString(keyBytes);
            System.out.println("Generated JWT Secret Key (Base64 Encoded):");
            System.out.println(base64Key);
            System.out.println("\nCopy this key and paste it into coursera.app.jwtSecret in application.properties.");
        }
    }
    