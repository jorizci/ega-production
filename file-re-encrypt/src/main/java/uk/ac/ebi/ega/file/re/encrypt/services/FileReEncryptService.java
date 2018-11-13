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
package uk.ac.ebi.ega.file.re.encrypt.services;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.database.commons.models.EgaPublishedFile;
import uk.ac.ebi.ega.database.commons.models.ReEncryptDataset;
import uk.ac.ebi.ega.database.commons.models.ReEncryptionFile;
import uk.ac.ebi.ega.database.commons.services.PeaService;
import uk.ac.ebi.ega.database.commons.services.ProFilerService;
import uk.ac.ebi.ega.database.commons.services.ReEncryptService;
import uk.ac.ebi.ega.encryption.core.ReEncryption;
import uk.ac.ebi.ega.encryption.core.ReEncryptionReport;
import uk.ac.ebi.ega.encryption.core.exception.OutputFileAlreadyExists;
import uk.ac.ebi.ega.file.re.encrypt.exceptions.OriginalEncryptedMd5Mismatch;
import uk.ac.ebi.ega.file.re.encrypt.properties.FileReEncryptProperties;
import uk.ac.ebi.ega.file.re.encrypt.services.fire.FireService;
import uk.ac.ebi.ega.file.re.encrypt.services.fire.IFireFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    private ProFilerService proFilerService;

    private PeaService peaService;

    private FireService fireService;

    private IKeyGenerator keyGenerator;

    public FileReEncryptService(FileReEncryptProperties properties, ReEncryptService reEncryptService,
                                ProFilerService proFilerService, PeaService peaService, FireService fireService,
                                IKeyGenerator keyGenerator) {
        this.properties = properties;
        this.reEncryptService = reEncryptService;
        this.proFilerService = proFilerService;
        this.peaService = peaService;
        this.fireService = fireService;
        this.keyGenerator = keyGenerator;
    }

    public void reEncryptDataset(String egaId) {
        final List<EgaPublishedFile> datasetFiles = peaService.getPublishedFiles(egaId);
        if (datasetFiles.isEmpty()) {
            logger.info("No files where found on dataset {}", egaId);
            return;
        }

        logger.info("Starting re encryption process for dataset {}, total files to re-encrypt {}", egaId,
                datasetFiles.size());
        int totalSuccesses = doReEncryptFiles(datasetFiles);
        doReport(egaId, datasetFiles, totalSuccesses);
    }

    private void doReport(String egaId, List<EgaPublishedFile> datasetFiles, int totalSuccesses) {
        if (totalSuccesses == datasetFiles.size()) {
            logger.info("Re encryption process for dataset {} finished correctly.", egaId);
        } else {
            logger.error("Re encryption process for dataset {} finished but {} were not processed correctly.", egaId,
                    datasetFiles.size() - totalSuccesses);
        }
        reEncryptService.insert(new ReEncryptDataset(egaId, totalSuccesses, datasetFiles.size()));
    }

    public int reEncryptFiles(String... fileIds) {
        final List<EgaPublishedFile> files = peaService.getPublishedFiles(fileIds);

        if (files.size() != fileIds.length) {
            logger.error("Could not find the following ids {} on Pea.", findMissingIds(fileIds, files));
            System.exit(1);
        }
        return doReEncryptFiles(files);
    }

    public int doReEncryptFiles(List<EgaPublishedFile> files) {
        int successfulReEncryptionProcesses = 0;
        for (EgaPublishedFile file : files) {
            final ReEncryptionFile reEncryptedFile = reEncryptService.findReEncryptedFile(file.getEgaId());
            if (reEncryptedFile != null) {
                logger.info("Skipping file {} re-encryption. It has been processed already.", file.getEgaId());
                doArchiveOfExistingReEncryptedFile(file, reEncryptedFile);
                successfulReEncryptionProcesses++;
            } else {
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
                } catch (OriginalEncryptedMd5Mismatch originalEncryptedMd5Mismatch) {
                    logger.error("Skipping file {}, md5 file mismatch.", file.getEgaId());
                }
            }
        }
        return successfulReEncryptionProcesses;
    }

    private void doArchiveOfExistingReEncryptedFile(EgaPublishedFile file, ReEncryptionFile reEncryptedFile) {
        if (reEncryptedFile.getFireArchiveId() != null) {
            logger.info("File {} has been inserted in pro-filer already", file.getEgaId());
        } else {
            Long proFilerId = insertIntoProFiler(reEncryptedFile.getEgaId(),
                    new File(reEncryptedFile.getNewPath()), reEncryptedFile.getStatus(),
                    reEncryptedFile.getEncryptedMd5());
            if (proFilerId != null) {
                reEncryptService.updateFireArchiveId(file.getEgaId(), proFilerId);
            }
        }
    }

    private List<String> findMissingIds(String[] fileIds, List<EgaPublishedFile> files) {
        List<String> missingIds = new ArrayList<>();
        Set<String> collect = files.stream().map(egaFile -> egaFile.getEgaId()).collect(Collectors.toSet());
        for (String fileId : fileIds) {
            if (!collect.contains(fileId)) {
                missingIds.add(fileId);
            }
        }
        return missingIds;
    }

    private void doReEncryptFile(EgaPublishedFile file) throws InvalidAlgorithmParameterException,
            InvalidKeyException, IOException, PGPException, OutputFileAlreadyExists, InvalidKeySpecException,
            OriginalEncryptedMd5Mismatch {
        File fileOut = generateFileOut(file);
        char[] originalPassword = getOriginalGpgPassword();
        char[] newPassword = keyGenerator.generateKey();
        boolean overrideFile = properties.isOverride();

        final IFireFile fileInFire = fireService.getFile(file);
        try (InputStream fileStream = fileInFire.getStream()) {
            final ReEncryptionReport report = ReEncryption.reEncrypt(fileStream, originalPassword, fileOut, newPassword,
                    overrideFile);
            ReEncryptionFile.ReEncryptionStatus status = calculateProcessStatus(file, fileInFire, report);
            Long proFilerId = insertIntoProFiler(file.getEgaId(), fileOut, status, report.getReEncryptedMd5());
            insertInReEncryption(file, fileOut, newPassword, proFilerId,
                    status, report);
        }
    }

    private File generateFileOut(EgaPublishedFile file) {
        return new File(properties.getOutputPath(), file.getEgaId() + file.getFileExtensions() + ".cip");
    }

    private char[] getOriginalGpgPassword() throws IOException {
        return new String(getGpgKeyFile()).trim().toCharArray();
    }

    private byte[] getGpgKeyFile() throws IOException {
        return Files.readAllBytes(new File(properties.getGpgKeyPath()).toPath());
    }

    private ReEncryptionFile.ReEncryptionStatus calculateProcessStatus(EgaPublishedFile file, IFireFile fileInFire,
                                                                       ReEncryptionReport report)
            throws OriginalEncryptedMd5Mismatch {
        if (fileInFire.getMd5() != null && !Objects.equals(fileInFire.getMd5(), report.getEncryptedMd5())) {
            throw new OriginalEncryptedMd5Mismatch();
        }
        if (Objects.equals(file.getUnencryptedMd5(), report.getUnencryptedMd5())) {
            return ReEncryptionFile.ReEncryptionStatus.CORRECT;
        } else {
            return ReEncryptionFile.ReEncryptionStatus.MISMATCH;
        }
    }

    private Long insertIntoProFiler(String egaId, File fileOut, ReEncryptionFile.ReEncryptionStatus status,
                                    String reEncryptedMd5) {
        if (!properties.isInsertProfiler()) {
            logger.warn("Insert to pro-filer is disabled, file {} has not been inserted.", egaId);
            return null;
        }
        if (status == ReEncryptionFile.ReEncryptionStatus.MISMATCH) {
            logger.warn("File {} has been re-encrypted correctly but unencrypted md5 does not match. It will not be " +
                    "inserted into pro-filer", egaId);
            return null;
        }
        if (!fileOut.exists()) {
            logger.error("File {} could not be found at {}", egaId, fileOut.getAbsolutePath());
            return null;
        }

        // We are not gonna insert the file id on the file table at least at the moment.
        final long number = proFilerService.insertFile(null, fileOut, reEncryptedMd5);
        final long proFilerId = proFilerService.insertArchive(number, properties.getRelativePath(), fileOut,
                reEncryptedMd5);
        logger.info("File {} has been inserted into pro-filer.", egaId);
        return proFilerId;
    }

    private void insertInReEncryption(EgaPublishedFile file, File fileOut, char[] newPassword, Long fireArchiveId,
                                      ReEncryptionFile.ReEncryptionStatus status, ReEncryptionReport report) {
        reEncryptService.insert(new ReEncryptionFile(
                file.getEgaId(),
                file.getDatasetId(),
                report.getEncryptedMd5(),
                report.getUnencryptedMd5(),
                report.getReEncryptedMd5(),
                new String(newPassword),
                file.getFileName(),
                fileOut.getName(),
                fileOut.getAbsolutePath(),
                file.getSize(),
                fileOut.length(),
                fireArchiveId,
                status));
    }

}
