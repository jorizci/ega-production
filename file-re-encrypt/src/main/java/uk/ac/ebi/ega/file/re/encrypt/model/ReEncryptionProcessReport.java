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
package uk.ac.ebi.ega.file.re.encrypt.model;

public class ReEncryptionProcessReport {

    private final int success;

    private final int md5;

    private final int errors;

    private final int conflict;

    public ReEncryptionProcessReport(int success, int md5, int errors, int conflict) {
        this.success = success;
        this.md5 = md5;
        this.errors = errors;
        this.conflict = conflict;
    }

    public int getSuccess() {
        return success;
    }

    public int getMd5() {
        return md5;
    }

    public int getErrors() {
        return errors;
    }

    public int getConflict() {
        return conflict;
    }
}
