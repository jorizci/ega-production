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
package uk.ac.ebi.ega.file.re.encrypted.publisher.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.database.commons.models.ReEncryptionFile;
import uk.ac.ebi.ega.database.commons.services.AuditService;
import uk.ac.ebi.ega.database.commons.services.PeaService;
import uk.ac.ebi.ega.database.commons.services.ProFilerService;
import uk.ac.ebi.ega.database.commons.services.ReEncryptService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PublishReEncryptedFilesService {

    private final static Logger logger = LoggerFactory.getLogger(PublishReEncryptedFilesService.class);

    private AuditService auditService;

    private ReEncryptService reEncryptService;

    private ProFilerService proFilerService;

    private PeaService peaService;

    public PublishReEncryptedFilesService(AuditService auditService, ReEncryptService reEncryptService,
                                          ProFilerService proFilerService, PeaService peaService) {
        this.reEncryptService = reEncryptService;
        this.auditService = auditService;
        this.proFilerService = proFilerService;
        this.peaService = peaService;
    }

    public void publish(String egaId) {
        final List<String> fileIds = auditService.getDatasetFiles(egaId);
        if (fileIds.isEmpty()) {
            logger.info("Dataset {} does not have files", egaId);
            return;
        }

        final List<ReEncryptionFile> files = reEncryptService.findReEncryptedFiles(fileIds);
        if (checkAllFilesAreEncryptedAndArchived(fileIds, files)) {
            doPublish(files);
        }
    }

    private boolean checkAllFilesAreEncryptedAndArchived(List<String> fileIds, List<ReEncryptionFile> files) {
        // Use the not short circuited boolean operation to get a full report
        return checkAllFilesArePresent(fileIds, files) & checkAllFilesAreReEncrypted(files) &
                checkAllFilesAreArchived(files);
    }

    private boolean checkAllFilesArePresent(List<String> fileIds, List<ReEncryptionFile> files) {
        final Set<String> reEncryptedFileIds = files.stream().map(reEncryptionFile -> reEncryptionFile.getEgaId())
                .collect(Collectors.toSet());
        boolean result = true;
        for (String fileId : fileIds) {
            if (!reEncryptedFileIds.contains(fileId)) {
                result = false;
                logger.error("File {} has not been re-encrypted.", fileId);
            }
        }
        return result;
    }

    private boolean checkAllFilesAreReEncrypted(List<ReEncryptionFile> files) {
        boolean result = true;
        for (ReEncryptionFile file : files) {
            if (file.getStatus() == ReEncryptionFile.ReEncryptionStatus.MISMATCH) {
                result = false;
                logger.error("File {} md5 did not match correctly.", file.getEgaId());
            }
        }
        return result;
    }

    private boolean checkAllFilesAreArchived(List<ReEncryptionFile> files) {
        boolean result = true;
        final List<Long> fireArchiveIds = files.stream().map(reEncryptionFile -> reEncryptionFile.getFireArchiveId())
                .filter(Objects::nonNull).collect(Collectors.toList());
        final Map<Long, Integer> fireIdsExitCodes = proFilerService.getFireIdsExitCodes(fireArchiveIds);
        for (ReEncryptionFile file : files) {
            if (file.getFireArchiveId() == null) {
                result = false;
                logger.error("File {} is not linked to a FireId.", file.getEgaId());
                continue;
            }
            final Integer fireExitCode = fireIdsExitCodes.get(file.getFireArchiveId());
            if (fireExitCode == null) {
                result = false;
                logger.error("File {} with fire archive id {} is not archived yet.", file.getEgaId(),
                        file.getFireArchiveId());
                continue;
            }
            if (fireExitCode != 0) {
                result = false;
                logger.error("File {} with fire archive id {} was not archived properly with code {}.",
                        file.getEgaId(), file.getFireArchiveId(), fireExitCode);
            }
        }
        return result;
    }

    private void doPublish(List<ReEncryptionFile> files) {
        peaService.updateFileName(files);
        auditService.updateFileName(files);
    }

}
