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
package uk.ac.ebi.ega.file.re.encryption.services;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.encryption.core.ReEncryption;
import uk.ac.ebi.ega.encryption.core.ReEncryptionReport;
import uk.ac.ebi.ega.encryption.core.exception.OutputFileAlreadyExists;
import uk.ac.ebi.ega.file.re.encryption.models.EgaAuditFile;
import uk.ac.ebi.ega.file.re.encryption.models.ReEncryptDataset;
import uk.ac.ebi.ega.file.re.encryption.models.ReEncryptionFile;
import uk.ac.ebi.ega.file.re.encryption.properties.FileReEncryptProperties;
import uk.ac.ebi.ega.file.re.encryption.utils.RandomStringGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class FileReEncryptService {

    private final Logger logger = LoggerFactory.getLogger(FileReEncryptService.class);

    private FileReEncryptProperties properties;

    private ReEncryptService reEncryptService;

    private AuditService auditService;

    private ProFilerService proFilerService;

    public FileReEncryptService(FileReEncryptProperties properties, ReEncryptService reEncryptService,
                                AuditService auditService, ProFilerService proFilerService) {
        this.properties = properties;
        this.reEncryptService = reEncryptService;
        this.auditService = auditService;
        this.proFilerService = proFilerService;
    }

    public void reEncryptDataset(String egaId) {
        final List<String> datasetFiles = auditService.getDatasetFiles(egaId);
        logger.info("Starting re encryption process for dataset {}, total files to re-encrypt {}", egaId,
                datasetFiles.size());

        int totalSuccesses = reEncryptFiles(datasetFiles.toArray(new String[datasetFiles.size()]));
        doReport(egaId, datasetFiles, totalSuccesses);
    }

    private void doReport(String egaId, List<String> datasetFiles, int totalSuccesses) {
        if (totalSuccesses == datasetFiles.size()) {
            logger.info("Re encryption process for dataset {} finished correctly.", egaId);
        } else {
            logger.error("Re encryption process for dataset {} finished but {} were not processed correctly.", egaId,
                    datasetFiles.size() - totalSuccesses);
        }
        reEncryptService.insert(new ReEncryptDataset(egaId, totalSuccesses, datasetFiles.size()));
    }

    public int reEncryptFiles(String... fileIds) {
        final List<EgaAuditFile> files = auditService.getFiles(fileIds);

        if (files.size() != fileIds.length) {
            logger.error("Could not find the following ids {} on Audit.", findMissingIds(fileIds, files));
            System.exit(1);
        }

        int successfulReEncryptionProcesses = 0;
        for (EgaAuditFile file : files) {
            if (reEncryptService.hasThisFileBeenProcessed(file.getEgaId())) {
                logger.info("Skipping file {}, it has been processed already.", file.getEgaId());
                successfulReEncryptionProcesses++;
                continue;
            }
            try {
                doReEncryptFile(file);
                successfulReEncryptionProcesses++;
            } catch (InvalidAlgorithmParameterException | InvalidKeyException | IOException | PGPException |
                    InvalidKeySpecException e) {
                logger.error(e.getMessage(), e);
                logger.error("Skipping file {}, due to major error.", file.getEgaId());
            } catch (OutputFileAlreadyExists outputFileAlreadyExists) {
                logger.error("Skipping file {}, re encrypted file already exists on result path. If you want to " +
                        "override it, please enable option 'file-re-encrypt.config.override'.", file.getEgaId());
            }
        }
        return successfulReEncryptionProcesses;
    }

    private List<String> findMissingIds(String[] fileIds, List<EgaAuditFile> files) {
        List<String> missingIds = new ArrayList<>();
        Set<String> collect = files.stream().map(egaAuditFile -> egaAuditFile.getEgaId()).collect(Collectors.toSet());
        for (String fileId : fileIds) {
            if (!collect.contains(fileId)) {
                missingIds.add(fileId);
            }
        }
        return missingIds;
    }

    private void doReEncryptFile(EgaAuditFile file) throws InvalidAlgorithmParameterException,
            InvalidKeyException, IOException, PGPException, OutputFileAlreadyExists, InvalidKeySpecException {
        File fileOnFire = new File(properties.getFirePath() + file.getSubmittedFileName());
        File fileOut = new File(properties.getOutputPath(), file.getEgaId() + ".cip");
        char[] originalPassword = new String(getGpgKeyFile()).trim().toCharArray();
        char[] newPassword = RandomStringGenerator.generateRandomString(properties.getRandomKeySize());
        boolean overrideFile = properties.isOverride();

        final ReEncryptionReport report = ReEncryption.reEncrypt(fileOnFire, originalPassword, fileOut, newPassword,
                overrideFile);

        if (Objects.equals(file.getUnencryptedMd5(), report.getUnencryptedMd5())) {
            doMd5Match(file, fileOut, newPassword, report);
        } else {
            doMd5Mismatch(file, fileOut, newPassword, report);
        }
    }

    private void doMd5Mismatch(EgaAuditFile file, File fileOut, char[] newPassword, ReEncryptionReport report) {
        logger.warn("File {} re encrypted successfully but there was a md5 mismatch", file.getEgaId());
        reEncryptService.insert(new ReEncryptionFile(
                file.getEgaId(),
                report.getEncryptedMd5(),
                report.getUnencryptedMd5(),
                report.getReEncryptedMd5(),
                new String(newPassword),
                fileOut.getAbsolutePath(),
                ReEncryptionFile.ReEncryptionStatus.MISMATCH));
    }

    private void doMd5Match(EgaAuditFile file, File fileOut, char[] newPassword, ReEncryptionReport report) {
        logger.info("File {} re encrypted successfully", file.getEgaId());
        reEncryptService.insert(new ReEncryptionFile(
                file.getEgaId(),
                report.getEncryptedMd5(),
                report.getUnencryptedMd5(),
                report.getReEncryptedMd5(),
                new String(newPassword),
                fileOut.getAbsolutePath(),
                ReEncryptionFile.ReEncryptionStatus.CORRECT));
        if (properties.isInsertProfiler()) {
            logger.info("File {} size.", fileOut.length());
            final Number number = proFilerService.insertFile(file.getEgaId(), fileOut, report.getReEncryptedMd5());
            proFilerService.insertArchive(number, properties.getRelativePath(), fileOut, report.getReEncryptedMd5());
            logger.info("File {} has been inserted into pro-filer.", file.getEgaId());
        } else {
            logger.warn("Insert to pro-filer is disabled, file {} has not been inserted.", file.getEgaId());
        }
    }

    private byte[] getGpgKeyFile() throws IOException {
        return Files.readAllBytes(new File(properties.getGpgKeyPath()).toPath());
    }

}
