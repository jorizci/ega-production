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

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.ega.database.commons.models.ReEncryptDataset;
import uk.ac.ebi.ega.database.commons.models.ReEncryptionFile;
import uk.ac.ebi.ega.database.commons.utils.Batch;

import java.util.Arrays;
import java.util.List;

public class ReEncryptService {

    private static final int BATCH = 1000;

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
                "dataset_id, " +
                "original_encrypted_md5, " +
                "plain_md5, " +
                "encrypted_md5, " +
                "\"key\", " +
                "original_path, " +
                "file_name, " +
                "new_path, " +
                "previous_size, " +
                "\"size\", " +
                "creation_date, " +
                "status, " +
                "pro_fire_archive_id) " +
                "VALUES(" +
                ":file_id, " +
                ":dataset_id, " +
                ":original_encrypted_md5, " +
                ":plain_md5, " +
                ":encrypted_md5, " +
                ":key, " +
                ":original_path, " +
                ":file_name, " +
                ":new_path, " +
                ":previous_size, " +
                ":size, " +
                ":creation_date, " +
                ":status::reencryption_status," +
                ":pro_fire_archive_id)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_id", file.getEgaId());
        parameters.addValue("dataset_id", file.getDatasetId());
        parameters.addValue("original_encrypted_md5", file.getOriginalEncryptedMd5());
        parameters.addValue("plain_md5", file.getPlainMd5());
        parameters.addValue("encrypted_md5", file.getEncryptedMd5());
        parameters.addValue("key", file.getKey());
        parameters.addValue("original_path", file.getOriginalPath());
        parameters.addValue("file_name", file.getName());
        parameters.addValue("new_path", file.getNewPath());
        parameters.addValue("previous_size", file.getPreviousSize());
        parameters.addValue("size", file.getSize());
        parameters.addValue("creation_date", file.getCreationDate());
        parameters.addValue("status", file.getStatus().toString());
        parameters.addValue("pro_fire_archive_id", file.getFireArchiveId());
        reEncryptTemplate.update(query, parameters);
    }

    public void insert(ReEncryptDataset reEncryptDataset) {
        String query = "INSERT INTO re_encryption.processed_datasets (" +
                "dataset_id, " +
                "total_files, " +
                "success, " +
                "wrong_md5, " +
                "conflict, " +
                "errors" +
                ") " +
                "VALUES(" +
                ":dataset_id, " +
                ":total_files, " +
                ":success, " +
                ":wrong_md5, " +
                ":conflict, " +
                ":errors" +
                ") " +
                "ON CONFLICT (dataset_id) " +
                "DO " +
                "UPDATE " +
                "SET " +
                "total_files = :total_files, " +
                "success = :success, " +
                "wrong_md5 = :wrong_md5, " +
                "conflict = :conflict, " +
                "errors = :errors";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("dataset_id", reEncryptDataset.getEgaId());
        parameters.addValue("total_files", reEncryptDataset.getFileTotal());
        parameters.addValue("success", reEncryptDataset.getSuccessTotal());
        parameters.addValue("wrong_md5", reEncryptDataset.getWrongMd5Total());
        parameters.addValue("conflict", reEncryptDataset.getConflictTotal());
        parameters.addValue("errors", reEncryptDataset.getErrorTotal());
        reEncryptTemplate.update(query, parameters);
    }

    public ReEncryptionFile findReEncryptedFile(String egaFileId) {
        List<String> ids = Arrays.asList(egaFileId);
        final List<ReEncryptionFile> reEncryptionFiles = doFindReEncryptedFilesBatch(ids);
        if (reEncryptionFiles.isEmpty()) {
            return null;
        } else {
            return reEncryptionFiles.get(0);
        }
    }

    public List<ReEncryptionFile> findReEncryptedFiles(List<String> egaFileIds) {
        final List<ReEncryptionFile> files = Batch.doInBatches(egaFileIds, this::doFindReEncryptedFilesBatch, BATCH);
        return files;
    }

    private List<ReEncryptionFile> doFindReEncryptedFilesBatch(List<String> egaFileIds) {
        String query = "SELECT " +
                "file_id, " +
                "dataset_id, " +
                "original_encrypted_md5, " +
                "plain_md5, " +
                "encrypted_md5, " +
                "\"key\", " +
                "original_path, " +
                "file_name,        " +
                "new_path, " +
                "previous_size, " +
                "\"size\", " +
                "creation_date, " +
                "status, " +
                "pro_fire_archive_id " +
                "FROM re_encryption.processed_files WHERE file_id IN (:fileIds)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("fileIds", egaFileIds);
        List<ReEncryptionFile> files = reEncryptTemplate.query(query, parameters, (resultSet, i) ->
                new ReEncryptionFile(
                        resultSet.getString("file_id"),
                        resultSet.getString("dataset_id"),
                        resultSet.getString("original_encrypted_md5"),
                        resultSet.getString("plain_md5"),
                        resultSet.getString("encrypted_md5"),
                        resultSet.getString("key"),
                        resultSet.getString("original_path"),
                        resultSet.getString("file_name"),
                        resultSet.getString("new_path"),
                        resultSet.getLong("previous_size"),
                        resultSet.getLong("size"),
                        resultSet.getObject("pro_fire_archive_id") != null ? resultSet.getLong("pro_fire_archive_id") :
                                null,
                        ReEncryptionFile.ReEncryptionStatus.valueOf(resultSet.getString("status")),
                        resultSet.getTimestamp("creation_date"))
        );
        return files;
    }

    public void updateFireArchiveId(String egaId, Long proFilerId) {
        String query = "UPDATE re_encryption.processed_files " +
                "SET pro_fire_archive_id=:pro_filer_id " +
                "WHERE file_id=:file_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_id", egaId);
        parameters.addValue("pro_filer_id", proFilerId);
        reEncryptTemplate.update(query, parameters);
    }

    public void deleteFile(ReEncryptionFile reEncryptedFile) {
        String query = "DELETE FROM re_encryption.processed_files " +
                "WHERE file_id=:file_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_id", reEncryptedFile.getEgaId());
        reEncryptTemplate.update(query, parameters);
    }
}
