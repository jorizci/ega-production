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
package uk.ac.ebi.ega.cmd.encryption.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.cmd.encryption.options.OutputFormat;
import uk.ac.ebi.ega.cmd.encryption.services.fire.FireService;
import uk.ac.ebi.ega.cmd.encryption.services.fire.IFireFile;
import uk.ac.ebi.ega.encryption.core.ReEncryption;
import uk.ac.ebi.ega.encryption.core.encryption.AesCbcOpenSSL;
import uk.ac.ebi.ega.encryption.core.encryption.AesCtr256Ega;
import uk.ac.ebi.ega.encryption.core.encryption.EncryptionAlgorithm;
import uk.ac.ebi.ega.encryption.core.encryption.PgpSymmetric;
import uk.ac.ebi.ega.encryption.core.encryption.Plain;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;

public class FileReEncryptService {

    private final Logger logger = LoggerFactory.getLogger(FileReEncryptService.class);

    private FireService fireService;

    public FileReEncryptService(FireService fireService) {
        this.fireService = fireService;
    }

    public void reEncryptFile(String fireFilePath, String outputPath, OutputFormat outputFormat, boolean useFireMount,
                              int retries, char[] password, char[] outputPassword) throws IOException {

        final EncryptionAlgorithm decryptionAlgorithm = getDecryptionAlgorithmFromExtension(fireFilePath);
        final EncryptionAlgorithm encryptionAlgorithm = getEncryptionAlgorithmFromOutputFormat(outputFormat);

        final IFireFile file = fireService.getFile(fireFilePath, !useFireMount);
        final String fileOutputPath = generateFileOutputPath(outputPath, fireFilePath, outputFormat);

        for (int i = 0; i <= retries; i++) {
            try (
                    InputStream fireFile = file.getStream();
                    OutputStream outputFile = new FileOutputStream(fileOutputPath)
            ) {
                ReEncryption.loggedReEncrypt(fireFile, password, decryptionAlgorithm, outputFile, outputPassword,
                        encryptionAlgorithm);
            } catch (IOException | RuntimeException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private EncryptionAlgorithm getDecryptionAlgorithmFromExtension(String filePath) {
        if (filePath.endsWith(".cip")) {
            return new AesCtr256Ega();
        } else if (filePath.endsWith(".gpg")) {
            return new PgpSymmetric();
        } else {
            throw new UnsupportedOperationException("Encryption could not be inferred from filepath '" + filePath + "'");
        }
    }

    private EncryptionAlgorithm getEncryptionAlgorithmFromOutputFormat(OutputFormat outputFormat) {
        switch (outputFormat) {
            case PLAIN:
                return new Plain();
            case AES_CBC_OPENSSL:
                return new AesCbcOpenSSL();
            case AES_CTR_256_EGA:
                return new AesCtr256Ega();
            default:
                throw new UnsupportedOperationException("Unknown output format");
        }
    }

    private String generateFileOutputPath(String outputPath, String fireFilePath, OutputFormat outputFormat) {
        String filename = removeEncryptionExtension(Paths.get(fireFilePath).getFileName().toString());
        switch (outputFormat) {
            case PLAIN:
                break;
            case AES_CBC_OPENSSL:
            case AES_CTR_256_EGA:
                filename = filename + ".cip";
                break;
        }
        return Paths.get(outputPath, filename).toString();
    }

    private String removeEncryptionExtension(String filename) {
        if (filename.endsWith(".cip") || filename.endsWith(".gpg")) {
            return filename.substring(0, filename.length() - 4);
        }
        return filename;
    }

}
