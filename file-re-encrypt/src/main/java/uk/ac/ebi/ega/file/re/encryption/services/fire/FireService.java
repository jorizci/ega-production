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
package uk.ac.ebi.ega.file.re.encryption.services.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.ega.file.re.encryption.models.EgaAuditFile;
import uk.ac.ebi.ega.file.re.encryption.properties.FireProperties;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

public class FireService {

    private static final Logger logger = LoggerFactory.getLogger(FireService.class);

    private static final String GET_URL = "OBJECT_GET";

    private static final String OBJECT_MD5 = "OBJECT_MD5";

    private FireProperties fireProperties;

    public FireService(FireProperties fireProperties) {
        this.fireProperties = fireProperties;
    }

    public IFireFile getFile(EgaAuditFile file) throws FileNotFoundException {
        if (fireProperties.isUseDirect()) {
            HttpURLConnection connection = null;
            try {
                connection = prepareConnection(file, new URL(fireProperties.getUrl()));
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Map<String, String> map = parseConnection(connection);
                    return new DirectFireFile(map.get(GET_URL), map.get(OBJECT_MD5));
                } else {
                    logger.info("Fire Direct returned {}", connection.getResponseCode());
                }
            } catch (IOException | ParseException e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else {
            return new FuseFireFile(fireProperties.getMountPath(), file.getSubmittedFileName());
        }
        throw new FileNotFoundException("File " + file.getSubmittedFileName() + " could not be accessed " +
                "through Fire Direct");
    }

    private Map<String, String> parseConnection(HttpURLConnection con) throws IOException, ParseException {
        Map<String, String> map = new HashMap<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
            String line;
            StringBuffer buffer = new StringBuffer();
            while ((line = in.readLine()) != null) {
                buffer.append(line);
                final String[] fields = line.split(" ");
                if (fields.length == 1) {
                    continue;
                }
                if (fields.length > 2) {
                    throw new ParseException(line, 0);
                }
                map.put(fields[0], fields[1]);
            }
            if (!(map.containsKey(GET_URL) && map.containsKey(OBJECT_MD5))) {
                throw new ParseException(buffer.toString(), 0);
            }
        }
        return map;
    }

    private HttpURLConnection prepareConnection(EgaAuditFile file, URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("X-FIRE-Archive", "ega");
        con.setRequestProperty("X-FIRE-Key", fireProperties.getKey());
        con.setRequestProperty("X-FIRE-FilePath", file.getSubmittedFileName());
        return con;
    }
}
