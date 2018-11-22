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
import uk.ac.ebi.ega.encryption.core.encryption.AesAlexander;
import uk.ac.ebi.ega.encryption.core.encryption.PgpSymmetric;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class ReEncryption {

    private static final Logger logger = LoggerFactory.getLogger(ReEncryption.class);
    public static final int BUFFER_SIZE = 8192;
    public static final int GBYTE = 1024 * 1024 * 1024;
    //Delta size for report status, 1G
    private static final long DELTA_SIZE = GBYTE;

    public static ReEncryptionReport reEncrypt(InputStream inputFile, char[] passwordInput, OutputStream outputFile,
                                               char[] passwordOutput)
            throws IOException, PGPException, InvalidAlgorithmParameterException,
            InvalidKeySpecException, InvalidKeyException {

        MessageDigest messageDigestEncrypted = null;
        MessageDigest messageDigest = null;
        MessageDigest messageDigestReEncrypted = null;
        try {
            messageDigestEncrypted = MessageDigest.getInstance("MD5");
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigestReEncrypted = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unexpected error", e);
        }

        try (
                InputStream digestedInputStream = new DigestInputStream(inputFile, messageDigestEncrypted);
                InputStream decryptedStream = PgpSymmetric.decrypt(digestedInputStream, passwordInput);
                InputStream digestedDecryptedStream = new DigestInputStream(decryptedStream, messageDigest);
                OutputStream digestedOutputStream = new DigestOutputStream(outputFile, messageDigestReEncrypted);
                OutputStream cypherOutputStream = AesAlexander.encrypt(passwordOutput, digestedOutputStream);
        ) {
            final long unencryptedSize = doReEncryption(digestedDecryptedStream, cypherOutputStream);
            String encryptedMd5 = getNormalizedMd5(messageDigestEncrypted);
            String unencryptedMd5 = getNormalizedMd5(messageDigest);
            String reEncryptedMd5 = getNormalizedMd5(messageDigestReEncrypted);
            logger.info("EncryptedMd5 {}, Unencrypted Md5 {}, Re encrypted Md5 {}, unencrypted file size {} bytes",
                    encryptedMd5, unencryptedMd5, reEncryptedMd5, unencryptedSize);
            return new ReEncryptionReport(encryptedMd5, unencryptedMd5, reEncryptedMd5, unencryptedSize);
        }
    }

    private static long doReEncryption(InputStream digestedDecryptedStream, OutputStream cypherOutputStream)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = digestedDecryptedStream.read(buffer);
        long totalBytes = 0;
        long lastReported = 0;
        while (bytesRead != -1) {
            totalBytes += bytesRead;
            cypherOutputStream.write(buffer, 0, bytesRead);
            if (totalBytes >= lastReported + DELTA_SIZE) {
                lastReported = totalBytes;
                logger.info("Total size re-encrypted {} Gbytes", toGbytes(lastReported));
            }
            bytesRead = digestedDecryptedStream.read(buffer);
        }
        return totalBytes;
    }

    private static long toGbytes(long lastReported) {
        return lastReported / GBYTE;
    }

    private static String getNormalizedMd5(MessageDigest messageDigest) {
        return DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
    }
}
