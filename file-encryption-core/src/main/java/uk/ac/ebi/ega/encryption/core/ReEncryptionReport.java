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
package uk.ac.ebi.ega.encryption.core;

public class ReEncryptionReport {

    private final String encryptedMd5;

    private final String unencryptedMd5;

    private final String reEncryptedMd5;

    private final long unencryptedSize;

    public ReEncryptionReport(String encryptedMd5, String unencryptedMd5, String reEncryptedMd5, long unencryptedSize) {
        this.encryptedMd5 = encryptedMd5;
        this.unencryptedMd5 = unencryptedMd5;
        this.reEncryptedMd5 = reEncryptedMd5;
        this.unencryptedSize = unencryptedSize;
    }

    public String getUnencryptedMd5() {
        return unencryptedMd5;
    }

    public String getEncryptedMd5() {
        return encryptedMd5;
    }

    public String getReEncryptedMd5() {
        return reEncryptedMd5;
    }

    public long getUnencryptedSize() {
        return unencryptedSize;
    }
}
