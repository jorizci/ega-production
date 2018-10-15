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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.ega.file.re.encryption.models.EgaAuditFile;
import uk.ac.ebi.ega.file.re.encryption.utils.Batch;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuditService {

    private static final int BATCH = 1000;
    private final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private NamedParameterJdbcTemplate auditTemplate;

    public AuditService(NamedParameterJdbcTemplate auditTemplate) {
        this.auditTemplate = auditTemplate;
    }

    public List<EgaAuditFile> filterNewFiles(List<EgaAuditFile> files) {
        List<String> batch = new ArrayList<>(BATCH);
        Set<String> newFileNames = new HashSet<>();
        for (EgaAuditFile file : files) {
            if (batch.size() == BATCH) {
                newFileNames.addAll(doFilterNewFilesBatch(batch));
            }
            batch.add(file.getFullFilename());
        }
        newFileNames.addAll(doFilterNewFilesBatch(batch));

        return files.stream().filter(file -> newFileNames.contains(file.getFullFilename()))
                .collect(Collectors.toList());
    }

    private Set<String> doFilterNewFilesBatch(List<String> names) {
        String query = "SELECT submitted_file_name FROM audit_file WHERE submitted_file_name IN (:names)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("names", names);
        List<String> existingNames = auditTemplate.queryForList(query, parameters, String.class);
        logger.error(existingNames.size() + "");
        return new HashSet<>(existingNames);
    }

    public List<EgaAuditFile> getFiles(String... fileIds) {
        final List<EgaAuditFile> files = Batch.doInBatches(Arrays.asList(fileIds), this::doGetFileBatch, BATCH);
        logger.info("Number of files searched on Audit {}, number of files retrieved {}", fileIds.length, files.size());
        return files;
    }

    private List<EgaAuditFile> doGetFileBatch(List<String> fileIds) {
        String query = "SELECT " +
                "af.stable_id AS EGA_ID, " +
                "af.submitted_file_name AS FILE_NAME, " +
                "af.file_type AS FILE_TYPE, " +
                "af.staging_source AS BOX, " +
                "mu.md5_checksum AS UNENCRYPTED_MD5, " +
                "me.md5_checksum AS ENCRYPTED_MD5 " +
                "FROM audit_file af, audit_md5 mu, audit_md5 me " +
                "WHERE af.stable_id IN (:file_ids) " +
                "AND mu.process_step = \"Submitter unencrypted md5\" " +
                "AND af.stable_id = mu.file_stable_id " +
                "AND me.process_step = \"Submitter encrypted md5\" " +
                "AND af.stable_id = me.file_stable_id ";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("file_ids", fileIds);
        return auditTemplate.query(query, parameters, (resultSet, i) -> {
            try {
                return createFile(resultSet);
            } catch (IllegalArgumentException e) {
                logger.warn("Skipping element {}", e.getMessage());
                return null;
            }
        });
    }

    private EgaAuditFile createFile(ResultSet resultSet) throws SQLException {
        return new EgaAuditFile(
                resultSet.getString("EGA_ID"),
                resultSet.getString("FILE_NAME"),
                resultSet.getString("FILE_TYPE"),
                resultSet.getString("BOX"),
                resultSet.getString("UNENCRYPTED_MD5"),
                resultSet.getString("ENCRYPTED_MD5")
        );
    }

    public List<String> getDatasetFiles(String egaId) {
        String query = "SELECT " +
                "af.stable_id AS EGA_ID " +
                "FROM audit_file af, dataset d, packet p, packet_audit_file_linker pafl " +
                "WHERE d.dataset_id = p.dataset_id " +
                "AND p.stable_id = pafl.packet_stable_id " +
                "AND pafl.file_id = af.file_id " +
                "AND d.stable_id = :dataset_id";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("dataset_id", egaId);
        return auditTemplate.queryForList(query, parameters, String.class);
    }
}
