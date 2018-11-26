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
package uk.ac.ebi.ega.database.commons.models;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

public class EgaAuditFile {

    private static Logger logger = LoggerFactory.getLogger(EgaAuditFile.class);

    private String egaId;

    private String submittedFileName;

    private String fullFilename;

    private String box;

    private String unencryptedMd5;

    private String encryptedMd5;

    private String fileType;

    private long fileSize;

    //TODO unit test this creation
    public EgaAuditFile(String egaId, String filename, String fileType, String box, String unencryptedMd5,
                        String encryptedMd5, long fileSize) {
        Assert.hasLength(egaId, "EgaId is missing");
        Assert.hasLength(filename, "EgaId " + egaId + " does not have a file connected to it.");
        this.egaId = egaId;
        this.submittedFileName = addGpgToFileNameIfNeeded(filename);
        this.fullFilename = generateFullFileName(egaId, filename);
        if (fileType == null || fileType.isEmpty()) {
            this.fileType = generateFileTypeFromFileName(filename);
        } else {
            this.fileType = fileType;
        }
        this.box = box;
        this.unencryptedMd5 = unencryptedMd5;
        this.encryptedMd5 = encryptedMd5;
        this.fileSize = fileSize;
    }

    private static String generateFileTypeFromFileName(String filename) {
        String components[] = filename.split("\\.");

        for (int i = components.length - 1; i > 0; i--) {
            switch (components[i]) {
                case "cip":
                case "gpg":
                case "gz":
                    continue;
                default:
                    logger.info("Generated file type {} from file name {}", components[i], filename);
                    return components[i];
            }
        }
        logger.warn("Could not generate file type from file name {}", filename);
        return null;
    }

    private String generateFullFileName(String egaId, String filename) {
        String temp = (egaId + "/" + filename).replaceAll("//", "/");
        return addGpgToFileNameIfNeeded(temp);
    }

    private String addGpgToFileNameIfNeeded(String fileName) {
        if (fileName.endsWith(".gpg") || fileName.endsWith(".cip")) {
            return fileName;
        }
        return fileName + ".gpg";
    }

    public String getEgaId() {
        return egaId;
    }

    public String getSubmittedFileName() {
        return submittedFileName;
    }

    public String getFullFilename() {
        return fullFilename;
    }

    public String getBox() {
        return box;
    }

    public String getUnencryptedMd5() {
        return unencryptedMd5;
    }

    public String getEncryptedMd5() {
        return encryptedMd5;
    }

    public String getFileType() {
        return fileType;
    }

    public boolean isUnencryptedMd5Valid() {
        return validateMd5(unencryptedMd5);
    }

    private boolean validateMd5(String md5) {
        return md5 != null && md5.length() == 32 && md5.toLowerCase().equals(md5);
    }

    public boolean isEncryptedMd5Valid() {
        return validateMd5(encryptedMd5);
    }

    public long getFileSize() {
        return fileSize;
    }

}
