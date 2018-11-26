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
package uk.ac.ebi.ega.file.re.encrypt.services.fire;

import java.io.IOException;
import java.io.InputStream;

public class FireDirectFile implements IFireFile {

    private final String url;

    private final String headUrl;

    private final String md5;

    public FireDirectFile(String url, String headUrl, String md5) {
        this.url = url;
        this.headUrl = headUrl;
        this.md5 = md5;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    @Override
    public InputStream getStream() throws IOException {
        return new FireDirectInputStream(url, headUrl);
    }

}
