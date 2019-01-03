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

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.cmd.encryption.options.OutputFormat;
import uk.ac.ebi.ega.cmd.encryption.services.fire.FireService;
import uk.ac.ebi.ega.cmd.encryption.services.fire.IFireFile;
import uk.ac.ebi.ega.encryption.core.Algorithms;
import uk.ac.ebi.ega.encryption.core.ReEncryption;
import uk.ac.ebi.ega.encryption.core.exceptions.UnknownFileExtension;
import uk.ac.ebi.ega.encryption.core.utils.FileUtils;

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
                              int retries, String passwordFile, String outputPasswordFile)
            throws UnknownFileExtension, IOException {

        Algorithms algorithm = Algorithms.fromExtension(fireFilePath);

        final char[] password = FileUtils.readFile(Paths.get(passwordFile));
        final char[] outputPassword = FileUtils.readFile(Paths.get(outputPasswordFile));

        final IFireFile file = fireService.getFile(fireFilePath, !useFireMount);
        final String fileOutputPath = generateFileOutputPath(outputPath, fireFilePath, outputFormat);

        for (int i = 0; i <= retries; i++) {
            try (
                    InputStream fireFile = file.getStream();
                    OutputStream outputFile = new FileOutputStream(fileOutputPath)
            ) {
                ReEncryption.reEncrypt(fireFile, password, outputFile, outputPassword, algorithm);
            } catch (PGPException | IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private String generateFileOutputPath(String outputPath, String fireFilePath, OutputFormat outputFormat) {
        String filename = removeEncryptionExtension(Paths.get(fireFilePath).getFileName().toString());
        switch (outputFormat) {
            case PLAIN:
                break;
            case GPG:
                filename = filename + ".gpg";
                break;
            case AES_JAG:
            case AES_ALEXANDER:
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
