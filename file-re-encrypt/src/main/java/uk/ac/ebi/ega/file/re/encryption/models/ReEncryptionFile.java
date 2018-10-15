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
package uk.ac.ebi.ega.file.re.encryption.models;

import java.sql.Timestamp;
import java.util.Date;

public class ReEncryptionFile {

    public enum ReEncryptionStatus {

        CORRECT,

        MISMATCH

    }

    private String egaId;

    private String originalEncryptedMd5;

    private String plainMd5;

    private String encryptedMd5;

    private String key;

    private String path;

    private Timestamp creationDate;

    private ReEncryptionStatus status;

    public ReEncryptionFile(String egaId, String originalEncryptedMd5, String plainMd5, String encryptedMd5,
                            String key, String path, ReEncryptionStatus status) {
        this.egaId = egaId;
        this.originalEncryptedMd5 = originalEncryptedMd5;
        this.plainMd5 = plainMd5;
        this.encryptedMd5 = encryptedMd5;
        this.key = key;
        this.path = path;
        this.creationDate = new Timestamp(new Date().getTime());
        this.status = status;
    }

    public String getEgaId() {
        return egaId;
    }

    public String getOriginalEncryptedMd5() {
        return originalEncryptedMd5;
    }

    public String getPlainMd5() {
        return plainMd5;
    }

    public String getEncryptedMd5() {
        return encryptedMd5;
    }

    public String getKey() {
        return key;
    }

    public String getPath() {
        return path;
    }

    public Timestamp getCreationDate() {
        return creationDate;
    }

    public ReEncryptionStatus getStatus() {
        return status;
    }
}
