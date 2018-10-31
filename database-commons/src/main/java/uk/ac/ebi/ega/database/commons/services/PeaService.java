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
package uk.ac.ebi.ega.database.commons.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import uk.ac.ebi.ega.database.commons.models.ReEncryptionFile;

import java.util.ArrayList;
import java.util.List;

public class PeaService {

    private final static Logger logger = LoggerFactory.getLogger(PeaService.class);

    private NamedParameterJdbcTemplate peaTemplate;

    public PeaService(NamedParameterJdbcTemplate peaTemplate) {
        this.peaTemplate = peaTemplate;
    }

    public int[] updateFileName(List<ReEncryptionFile> files) {
        String query = "UPDATE file SET file_name=:file_name WHERE stable_id=:stable_id";

        List<SqlParameterSource> parametersBatch = new ArrayList<>();
        for (ReEncryptionFile file : files) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("file_name", file.getName());
            parameters.addValue("stable_id", file.getEgaId());
            parametersBatch.add(parameters);
        }

        final int[] results = peaTemplate.batchUpdate(query, SqlParameterSourceUtils.createBatch(parametersBatch));
        for (int i = 0; i < results.length; i++) {
            if (results[i] != 1) {
                logger.error("Update to file {} in pea did not work properly with file name {}. Total update " +
                        "operations {}", files.get(i).getEgaId(), files.get(i).getName(), results[i]);
            }
        }
        return results;
    }
}
