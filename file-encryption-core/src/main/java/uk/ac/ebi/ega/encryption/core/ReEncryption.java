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
package uk.ac.ebi.ega.encryption.core;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.encryption.core.encryption.AesCtr;
import uk.ac.ebi.ega.encryption.core.encryption.PgpSymmetric;
import uk.ac.ebi.ega.encryption.core.utils.Hash;
import uk.ac.ebi.ega.encryption.core.utils.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.spec.InvalidKeySpecException;

public class ReEncryption {

    private static final Logger logger = LoggerFactory.getLogger(ReEncryption.class);

    public static final int BUFFER_SIZE = 8192;

    public static ReEncryptionReport reEncrypt(InputStream inputFile, char[] passwordInput, OutputStream outputFile,
                                               char[] passwordOutput, Algorithms decryptAlgorithm)
            throws IOException, PGPException, InvalidAlgorithmParameterException,
            InvalidKeySpecException, InvalidKeyException {

        MessageDigest messageDigestEncrypted = Hash.getMd5();
        MessageDigest messageDigest = Hash.getMd5();
        MessageDigest messageDigestReEncrypted = Hash.getMd5();

        long unencryptedSize;
        try (
                InputStream digestedInputStream = new DigestInputStream(inputFile, messageDigestEncrypted);
                InputStream decryptedStream = getDecryptAlgorithm(passwordInput, digestedInputStream, decryptAlgorithm);
                InputStream digestedDecryptedStream = new DigestInputStream(decryptedStream, messageDigest);
                OutputStream digestedOutputStream = new DigestOutputStream(outputFile, messageDigestReEncrypted);
                OutputStream cypherOutputStream = AesCtr.encrypt(passwordOutput, digestedOutputStream);
        ) {
            unencryptedSize = IOUtils.bufferedPipe(digestedDecryptedStream, cypherOutputStream, BUFFER_SIZE);
        }
        String encryptedMd5 = Hash.normalize(messageDigestEncrypted);
        String unencryptedMd5 = Hash.normalize(messageDigest);
        String reEncryptedMd5 = Hash.normalize(messageDigestReEncrypted);
        logger.info("EncryptedMd5 {}, Unencrypted Md5 {}, Re encrypted Md5 {}, unencrypted file size {} bytes",
                encryptedMd5, unencryptedMd5, reEncryptedMd5, unencryptedSize);
        return new ReEncryptionReport(encryptedMd5, unencryptedMd5, reEncryptedMd5, unencryptedSize);
    }

    private static InputStream getDecryptAlgorithm(char[] password, InputStream inputStream,
                                                   Algorithms decryptAlgorithm)
            throws IOException, PGPException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            InvalidKeyException {
        switch (decryptAlgorithm) {
            case AES:
                return AesCtr.decrypt(inputStream, password);
            case PGP:
                return PgpSymmetric.decrypt(inputStream, password);
            default:
                throw new RuntimeException("Missing decryption algorithm");
        }
    }

}
