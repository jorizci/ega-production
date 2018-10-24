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
package uk.ac.ebi.ega.file.re.encrypted.publisher.utils;

import java.util.concurrent.ThreadLocalRandom;

public class RandomStringGenerator {

    private static String dictionary = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";

    public static char[] generateRandomString(int size) {
        char[] randomString = new char[size];
        for (int i = 0; i < size; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, dictionary.length());
            randomString[i] = dictionary.charAt(randomNum);
        }
        return randomString;
    }

}
