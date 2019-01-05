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
package uk.ac.ebi.ega.encryption.core.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class FileUtils {

    public static char[] readFile(Path path) throws IOException {
        final byte[] bytes = Files.readAllBytes(path);
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) (bytes[i] & 0xFF);
        }
        return chars;
    }

    public static char[] trim(char[] array) {
        int charsToSkipAtBeginning = 0;
        int lastPositionToCopy = 0;

        for (int i = 0; i < array.length; i++) {
            if (array[i] == ' ' || array[i] == '\n' || array[i] == '\t') {
                charsToSkipAtBeginning++;
            } else {
                break;
            }
        }

        for (int i = charsToSkipAtBeginning; i < array.length; i++) {
            if (!(array[i] == ' ' || array[i] == '\n' || array[i] == '\t')) {
                lastPositionToCopy = i;
            } else {
                break;
            }
        }
        return Arrays.copyOfRange(array, charsToSkipAtBeginning, lastPositionToCopy + 1);
    }

    public static char[] readPasswordFile(Path path) throws IOException {
        return trim(readFile(path));
    }

}
