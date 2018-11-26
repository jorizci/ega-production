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
import uk.ac.ebi.ega.database.commons.models.EgaPublishedFile;
import uk.ac.ebi.ega.database.commons.models.ReEncryptionFile;
import uk.ac.ebi.ega.database.commons.utils.Batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PeaService {

    private final static int BATCH = 1000;

    private final static Logger logger = LoggerFactory.getLogger(PeaService.class);

    private NamedParameterJdbcTemplate peaTemplate;

    public PeaService(NamedParameterJdbcTemplate peaTemplate) {
        this.peaTemplate = peaTemplate;
    }

    public int[] updateFileNameAndSize(List<ReEncryptionFile> files) {
        String query = "UPDATE file SET file_name=:file_name, size=:size WHERE stable_id=:stable_id";

        List<SqlParameterSource> parametersBatch = new ArrayList<>();
        for (ReEncryptionFile file : files) {
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            parameters.addValue("file_name", file.getName());
            parameters.addValue("size", file.getSize());
            parameters.addValue("stable_id", file.getEgaId());
            parametersBatch.add(parameters);
        }

        return peaTemplate.batchUpdate(query, parametersBatch.toArray(new SqlParameterSource[]{}));
    }

    public List<EgaPublishedFile> getPublishedFiles(String egaId) {
        String query = "SELECT " +
                "dataset_stable_id, " +
                "file_name, " +
                "`size`, " +
                "stable_id, " +
                "unencrypted_md5 " +
                "FROM file " +
                "WHERE " +
                "dataset_stable_id=:dataset_stable_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("dataset_stable_id", egaId);
        return peaTemplate.query(query, parameters, this::createFile);
    }

    private EgaPublishedFile createFile(ResultSet resultSet, int i) {
        try {
            return new EgaPublishedFile(
                    resultSet.getString("dataset_stable_id"),
                    resultSet.getString("stable_id"),
                    resultSet.getString("file_name"),
                    resultSet.getLong("size"),
                    resultSet.getString("unencrypted_md5"));
        } catch (SQLException e) {
            logger.warn("Skipping element {}", e.getMessage());
            return null;
        }
    }

    public List<EgaPublishedFile> getPublishedFiles(String[] fileIds) {
        List<EgaPublishedFile> files = Batch.doInBatches(Arrays.asList(fileIds), this::doGetPublishedFiles, BATCH);
        logger.info("Number of files searched on Audit {}, number of files retrieved {}", fileIds.length, files.size());
        return files;
    }

    private List<EgaPublishedFile> doGetPublishedFiles(List<String> fileIds) {
        String query = "SELECT " +
                "dataset_stable_id, " +
                "file_name, " +
                "`size`, " +
                "stable_id, " +
                "unencrypted_md5 " +
                "FROM file " +
                "WHERE " +
                "stable_id IN (:file_ids)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_ids", fileIds);
        return peaTemplate.query(query, parameters, this::createFile);
    }

}
