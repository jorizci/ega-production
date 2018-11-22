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

    private final int total;

    private final int success;

    private final int wrongMd5;

    private final int conflict;

    private final int errors;

    public ReEncryptDataset(String egaId, int total, int success, int wrongMd5, int conflict, int errors) {
        this.egaId = egaId;
        this.total = total;
        this.success = success;
        this.wrongMd5 = wrongMd5;
        this.conflict = conflict;
        this.errors = errors;
    }

    public String getEgaId() {
        return egaId;
    }

    public int getSuccess() {
        return success;
    }

    public int getTotal() {
        return total;
    }

    public int getWrongMd5() {
        return wrongMd5;
    }

    public int getConflict() {
        return conflict;
    }

    public int getErrors() {
        return errors;
    }

}
