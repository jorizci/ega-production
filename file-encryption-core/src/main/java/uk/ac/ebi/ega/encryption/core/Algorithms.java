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

import uk.ac.ebi.ega.encryption.core.exceptions.UnknownFileExtension;

public enum Algorithms {

    AES("cip"),

    PGP("gpg");


    private final String fileExtension;

    Algorithms(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public static Algorithms fromExtension(String fileExtension) throws UnknownFileExtension {
        for(Algorithms algorithm: values()){
            if(fileExtension!=null && algorithm.fileExtension.compareToIgnoreCase(fileExtension)==0){
                return algorithm;
            }
        }
        throw new UnknownFileExtension(fileExtension);
    }

}
