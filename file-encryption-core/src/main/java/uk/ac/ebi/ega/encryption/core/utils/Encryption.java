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
package uk.ac.ebi.ega.encryption.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Encryption {

    private final static Logger logger = LoggerFactory.getLogger(Encryption.class);



    /**
     * Returns configured cipher following the specification provided. This method is only recommended for known
     * working specifications.
     *
     * @param algorithm
     * @param encryptMode
     * @param secretKey
     * @param ivParameterSpec
     * @return Cipher
     * @throws AssertionError if specification is invalid or algorithm could not be found
     */
    public static Cipher getCipher(String algorithm, int encryptMode, SecretKey secretKey,
                                   IvParameterSpec ivParameterSpec) {
        try {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(encryptMode, secretKey, ivParameterSpec);
            return cipher;
        } catch (NoSuchPaddingException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                InvalidKeyException e) {
            logger.error(e.getMessage(), e);
            throw new AssertionError(e);
        }
    }


}
