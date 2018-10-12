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
import uk.ac.ebi.ega.encryption.core.exception.OutputFileAlreadyExists;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

    public static ReEncryptionReport reEncrypt(File inputFile, char[] passwordInput, File outputFile,
                                               char[] passwordOutput, boolean overwrite)
            throws IOException, PGPException, OutputFileAlreadyExists, InvalidAlgorithmParameterException,
            InvalidKeySpecException, InvalidKeyException {

        if (!overwrite && outputFile.exists()) {
            throw new OutputFileAlreadyExists(outputFile);
        }

        MessageDigest messageDigest = null;
        MessageDigest messageDigestReEncrypted = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigestReEncrypted = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unexpected error", e);
        }

        try (
                InputStream inputStream = new FileInputStream(inputFile);
                InputStream decryptedStream = PgpSymmetric.decrypt(inputStream, passwordInput);
                InputStream digestedDecryptedStream = new DigestInputStream(decryptedStream, messageDigest);
                OutputStream outputStream = new FileOutputStream(outputFile);
                OutputStream digestedOutputStream = new DigestOutputStream(outputStream, messageDigestReEncrypted);
                OutputStream cypherOutputStream = AesAlexander.encrypt(passwordOutput, digestedOutputStream);
        ) {
            doReEncryption(digestedDecryptedStream, cypherOutputStream);
            String unencryptedMd5 = getNormalizedMd5(messageDigest);
            String reEncryptedMd5 = getNormalizedMd5(messageDigestReEncrypted);
            logger.info("Unencrypted Md5 {}, Re encrypted Md5 {}", unencryptedMd5, reEncryptedMd5);
            return new ReEncryptionReport(unencryptedMd5, reEncryptedMd5);
        }
    }

    private static void doReEncryption(InputStream digestedDecryptedStream, OutputStream cypherOutputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = digestedDecryptedStream.read(buffer);
        while (bytesRead != -1) {
            cypherOutputStream.write(buffer, 0, bytesRead);
            bytesRead = digestedDecryptedStream.read(buffer);
        }
    }

    private static String getNormalizedMd5(MessageDigest messageDigest) {
        return DatatypeConverter.printHexBinary(messageDigest.digest()).toLowerCase();
    }
}
