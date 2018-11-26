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
package uk.ac.ebi.ega.file.re.encrypt.properties;

import java.io.File;

public class FileReEncryptProperties {


    private String stagingPath;

    private String relativePath;

    private String gpgKeyPath;

    private boolean insertProfiler;

    public String getStagingPath() {
        return stagingPath;
    }

    public void setStagingPath(String stagingPath) {
        this.stagingPath = stagingPath;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public File getOutputPath() {
        return new File(relativePath, stagingPath);
    }

    public String getGpgKeyPath() {
        return gpgKeyPath;
    }

    public void setGpgKeyPath(String gpgKeyPath) {
        this.gpgKeyPath = gpgKeyPath;
    }

    public boolean isInsertProfiler() {
        return insertProfiler;
    }

    public void setInsertProfiler(boolean insertProfiler) {
        this.insertProfiler = insertProfiler;
    }

}
