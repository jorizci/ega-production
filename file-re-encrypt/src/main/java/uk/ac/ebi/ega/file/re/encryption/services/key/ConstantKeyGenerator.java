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
package uk.ac.ebi.ega.file.re.encryption.services.key;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConstantKeyGenerator implements IKeyGenerator {

    private final byte[] file;

    public ConstantKeyGenerator(String constantFile) throws IOException {
        this.file = readFile(constantFile);
    }

    @Override
    public char[] generateKey() {
        return new String(file).trim().toCharArray();
    }

    private static byte[] readFile(String file) throws IOException {
        return Files.readAllBytes(new File(file).toPath());
    }

}
