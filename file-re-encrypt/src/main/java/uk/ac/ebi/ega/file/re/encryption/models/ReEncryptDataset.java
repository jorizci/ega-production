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

public class ReEncryptDataset {

    private final String egaId;

    private final int totalSuccesses;

    private final int totalFiles;

    public ReEncryptDataset(String egaId, int totalSuccesses, int totalFiles) {
        this.egaId = egaId;
        this.totalSuccesses = totalSuccesses;
        this.totalFiles = totalFiles;
    }

    public String getEgaId() {
        return egaId;
    }

    public int getTotalSuccesses() {
        return totalSuccesses;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

}
