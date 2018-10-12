/*
 *
 * Copyright 2018 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ega.encryption.core.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Provides functionality to encrypt files using Alexander's AES flavour.
 */
public class AesAlexander {

    private static Logger logger = LoggerFactory.getLogger(AesAlexander.class);
    private static byte[] DEFAULT_SALT = new byte[]{-12, 34, 1, 0, -98, -33, 78, 21};
    private static int DEFAULT_ITERATION = 256;

    public static OutputStream encrypt(char[] password, OutputStream outputStream)
            throws InvalidAlgorithmParameterException, InvalidKeyException, IOException, InvalidKeySpecException {
        return encrypt(getKey(password, DEFAULT_SALT, DEFAULT_ITERATION), outputStream);
    }

    public static OutputStream encrypt(SecretKey secretKey, OutputStream outputStream)
            throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        byte[] randomBytes = new byte[16];
        SecureRandom secureRandom = null;
        Cipher cipher = null;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            logger.error("Unexpected error", e);
        }
        secureRandom.nextBytes(randomBytes);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(randomBytes);
        outputStream.write(randomBytes);
        cipher.init(1, secretKey, ivParameterSpec);
        return new CipherOutputStream(outputStream, cipher);
    }

    public static SecretKey getKey(char[] password, byte[] salt, int iterationCount) throws InvalidKeySpecException {
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unexpected error", e);
        }
        PBEKeySpec pBEKeySpec = new PBEKeySpec(password, salt, 1024, iterationCount);
        SecretKey secretKey = secretKeyFactory.generateSecret(pBEKeySpec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

}
