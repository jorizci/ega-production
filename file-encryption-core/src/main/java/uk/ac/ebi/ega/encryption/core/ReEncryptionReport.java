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

    private final String unencryptedMd5;
    private final String encryptedMd5;

    public ReEncryptionReport(String unencryptedMd5, String encryptedMd5) {
        this.unencryptedMd5 = unencryptedMd5;
        this.encryptedMd5 = encryptedMd5;
    }

    public String getUnencryptedMd5() {
        return unencryptedMd5;
    }

    public String getEncryptedMd5() {
        return encryptedMd5;
    }

}
