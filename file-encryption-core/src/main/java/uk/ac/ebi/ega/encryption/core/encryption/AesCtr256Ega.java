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
import uk.ac.ebi.ega.encryption.core.utils.Encryption;
import uk.ac.ebi.ega.encryption.core.utils.Random;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Provides functionality to encrypt files using Alexander's AES flavour.
 */
public class AesCtr256Ega extends AlgorithmGeneric {

    private static final Logger logger = LoggerFactory.getLogger(AesCtr256Ega.class);

    private static final int ITERATION_COUNT = 1024;

    private static int KEY_SIZE = 256;

    private static byte[] DEFAULT_SALT = new byte[]{-12, 34, 1, 0, -98, -33, 78, 21};

    private IvParameterSpec ivParameterSpec;

    private SecretKey secretKey;

    @Override
    protected void initializeRead(InputStream inputStream, char[] password) throws IOException {
        byte[] randomBytes = new byte[16];
        for (int i = 0; i < 16; i++) {
            int readValue = inputStream.read();
            if (readValue == -1) {
                throw new IOException("AES CTR stream ended unexpectedly before reading the header");
            }
            randomBytes[i] = (byte) readValue;
        }
        ivParameterSpec = new IvParameterSpec(randomBytes);
        secretKey = getKey(password, DEFAULT_SALT);
    }

    @Override
    protected void initializeWrite(char[] password, OutputStream outputStream) throws IOException {
        byte[] randomBytes = new byte[16];
        Random.getSHA1PRNG().nextBytes(randomBytes);
        outputStream.write(randomBytes);
        ivParameterSpec = new IvParameterSpec(randomBytes);
        secretKey = getKey(password, DEFAULT_SALT);
    }

    @Override
    protected Cipher getCipher(int encryptMode) {
        return Encryption.getCipher("AES/CTR/NoPadding", encryptMode, secretKey, ivParameterSpec);
    }

    public static SecretKey getKey(char[] password, byte[] salt) {
        SecretKeyFactory secretKeyFactory = null;
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unexpected error", e);
        }
        PBEKeySpec pBEKeySpec = new PBEKeySpec(password, salt, ITERATION_COUNT, KEY_SIZE);
        try {
            return new SecretKeySpec(secretKeyFactory.generateSecret(pBEKeySpec).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
            throw new AssertionError(e);
        }
    }

}
