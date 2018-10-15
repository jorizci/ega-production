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
package uk.ac.ebi.ega.file.re.encryption.services;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.ega.file.re.encryption.models.ReEncryptDataset;
import uk.ac.ebi.ega.file.re.encryption.models.ReEncryptionFile;

import java.util.List;

public class ReEncryptService {

    private NamedParameterJdbcTemplate reEncryptTemplate;

    public ReEncryptService(NamedParameterJdbcTemplate reEncryptTemplate) {
        this.reEncryptTemplate = reEncryptTemplate;
    }

    public boolean hasThisFileBeenProcessed(String egaId) {
        String query = "SELECT count(*) " +
                "FROM re_encryption.processed_files " +
                "WHERE file_id=:file_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_id", egaId);
        final List<Integer> integers = reEncryptTemplate.queryForList(query, parameters, Integer.class);
        return integers.get(0) == 1;
    }


    public void insert(ReEncryptionFile file) {
        String query = "INSERT INTO processed_files(" +
                "file_id, " +
                "original_encrypted_md5, " +
                "plain_md5, " +
                "encrypted_md5, " +
                "\"key\", " +
                "\"path\", " +
                "creation_date, " +
                "status) " +
                "VALUES(" +
                ":file_id, " +
                ":original_encrypted_md5, " +
                ":plain_md5, " +
                ":encrypted_md5, " +
                ":key, " +
                ":path, " +
                ":creation_date, " +
                ":status::reencryption_status)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_id", file.getEgaId());
        parameters.addValue("original_encrypted_md5", file.getOriginalEncryptedMd5());
        parameters.addValue("plain_md5", file.getPlainMd5());
        parameters.addValue("encrypted_md5", file.getEncryptedMd5());
        parameters.addValue("key", file.getKey());
        parameters.addValue("path", file.getPath());
        parameters.addValue("creation_date", file.getCreationDate());
        parameters.addValue("status", file.getStatus().toString());
        reEncryptTemplate.update(query, parameters);
    }

    public void insert(ReEncryptDataset reEncryptDataset) {
        String query = "INSERT INTO re_encryption.processed_datasets (" +
                "dataset_id, " +
                "total_files, " +
                "processed_files) " +
                "VALUES(" +
                ":dataset_id, " +
                ":total_files, " +
                ":processed_files " +
                ")";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("dataset_id", reEncryptDataset.getEgaId());
        parameters.addValue("total_files", reEncryptDataset.getTotalFiles());
        parameters.addValue("processed_files", reEncryptDataset.getTotalSuccesses());
        reEncryptTemplate.update(query, parameters);
    }
}
