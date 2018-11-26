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

public class ReEncryptDataset {

    private final String egaId;

    private final int fileTotal;

    private final int successTotal;

    private final int wrongMd5Total;

    private final int conflictTotal;

    private final int errorTotal;

    public ReEncryptDataset(String egaId, int totalFiles, int successTotal, int wrongMd5Total, int conflictTotal, int errorTotal) {
        this.egaId = egaId;
        this.fileTotal = totalFiles;
        this.successTotal = successTotal;
        this.wrongMd5Total = wrongMd5Total;
        this.conflictTotal = conflictTotal;
        this.errorTotal = errorTotal;
    }

    public String getEgaId() {
        return egaId;
    }

    public int getSuccessTotal() {
        return successTotal;
    }

    public int getFileTotal() {
        return fileTotal;
    }

    public int getWrongMd5Total() {
        return wrongMd5Total;
    }

    public int getConflictTotal() {
        return conflictTotal;
    }

    public int getErrorTotal() {
        return errorTotal;
    }

}
