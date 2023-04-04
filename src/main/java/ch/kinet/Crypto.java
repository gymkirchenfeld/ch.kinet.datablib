/*
 * Copyright (C) 2017 by Tom Jampen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY); without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ch.kinet;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;

public class Crypto {

    public static final String ALGORITHM = "AES";
    public static final int KEY_LENGTH = 256 / 8;

    public static SecretKeySpec generateKey(String secret) {
        SecretKeySpec secretKeySpec = null;

        try {
            byte[] secretBytes = secret.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(secretBytes);
            byte[] key = Arrays.copyOf(md.digest(), KEY_LENGTH);

            secretKeySpec = new SecretKeySpec(key, ALGORITHM);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new CryptoException(e);
        }

        return secretKeySpec;
    }

    public static String encrypt(String strToEncrypt, String secret) {
        if (strToEncrypt == null) {
            return null;
        }

        String base64enc = null;

        try {
            // Key
            SecretKeySpec secretKeySpec = generateKey(secret);

            // generate IV
            SecureRandom random = new SecureRandom();
            byte iv[] = new byte[16];
            random.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Cipher
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] payload = cipher.doFinal(strToEncrypt.getBytes("UTF-8"));

            // Base64
            byte[] encrypted = new byte[iv.length + payload.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(payload, 0, encrypted, iv.length, payload.length);
            base64enc = Base64.getEncoder().encodeToString(encrypted);
        }
        catch (Exception e) {
            throw new CryptoException(e);
        }

        return base64enc;
    }

    public static String decrypt(String strToDecrypt, String secret) {
        if (strToDecrypt == null) {
            return null;
        }

        if (strToDecrypt.startsWith("2-")) {
            strToDecrypt = strToDecrypt.substring(2, strToDecrypt.length());
        }
        
        return decryptNew(strToDecrypt, secret);
    }

    public static String decryptNew(String strToDecrypt, String secret) {
        String decrypted = null;

        try {
            // Key
            SecretKeySpec secretKeySpec = generateKey(secret);

            // Base64
            byte[] base64dec = Base64.getDecoder().decode(strToDecrypt);

            // setup Cipher
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

            // extract IV nad payload
            int ivLength = cipher.getBlockSize();
            byte[] iv = Arrays.copyOf(base64dec, ivLength);
            byte[] payload = Arrays.copyOfRange(base64dec, ivLength, base64dec.length);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Cipher
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            decrypted = new String(cipher.doFinal(payload));
        }
        catch (Exception e) {
            throw new CryptoException(e);
        }

        return decrypted;
    }

    public static String decryptOld(String strToDecrypt, String secret) {
        String decrypted = null;

        try {
            // Key
            byte[] key = secret.substring(0, 16).getBytes("UTF-8");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            // transform hex string to bytes
            byte[] bytesToDecrypt = hexStringToByteArray(strToDecrypt);

            // Cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decrypted = new String(cipher.doFinal(bytesToDecrypt));
        }
        catch (Exception e) {
            throw new CryptoException(e);
        }

        return decrypted;
    }

    public static byte[] hexStringToByteArray(String s) {
        byte[] b = new byte[s.length() / 2];

        for (int i = 0; i < b.length; i++) {
            int index = i * 2;
            int v = Integer.parseInt(s.substring(index, index + 2), 16);
            b[i] = (byte) v;
        }

        return b;
    }
}
