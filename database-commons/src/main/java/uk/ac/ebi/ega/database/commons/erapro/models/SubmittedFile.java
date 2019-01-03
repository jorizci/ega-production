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
package uk.ac.ebi.ega.database.commons.erapro.models;

import org.springframework.util.Assert;

public class SubmittedFile {

    private final String egaId;
    private final String filename;
    private final String box;
    private final String unencryptedMd5;
    private final String encryptedMd5;
    private final String fileType;

    public SubmittedFile(String egaId, String filename, String box, String unencryptedMd5, String encryptedMd5,
                         String fileType) {
        this.egaId = egaId;
        this.filename = filename;
        this.box = box;
        this.unencryptedMd5 = unencryptedMd5;
        this.encryptedMd5 = encryptedMd5;
        this.fileType = fileType;
        Assert.hasText(egaId, "Missing egaId");
        Assert.hasText(filename, "Missing filename");
        Assert.hasText(filename, "Missing box");
    }

    public String getEgaId() {
        return egaId;
    }

    public String getFilename() {
        return filename;
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
}
