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

public class EgaPublishedFile {

    private String datasetId;

    private String egaId;

    private String fileName;

    private long size;

    private String unencryptedMd5;

    public EgaPublishedFile(String datasetId, String egaId, String fileName, long size, String unencryptedMd5) {
        this.datasetId = datasetId;
        this.egaId = egaId;
        this.fileName = fileName;
        this.size = size;
        this.unencryptedMd5 = unencryptedMd5;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public String getEgaId() {
        return egaId;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize() {
        return size;
    }

    public String getUnencryptedMd5() {
        return unencryptedMd5;
    }

}
