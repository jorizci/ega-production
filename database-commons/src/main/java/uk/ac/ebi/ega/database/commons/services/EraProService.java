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
import org.springframework.jdbc.core.JdbcTemplate;
import uk.ac.ebi.ega.database.commons.models.EgaAuditFile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EraProService {

    private Logger logger = LoggerFactory.getLogger(EraProService.class);

    private JdbcTemplate eraproTemplate;

    public EraProService(JdbcTemplate eraproTemplate) {
        this.eraproTemplate = eraproTemplate;
    }

    public List<EgaAuditFile> getFilesLinkedToRunsInTheLastDays(long days) {
        String query = "SELECT " +
                "r.EGA_ID, " +
                "rf.xml_filename as FILENAME, " +
                "esa.dir_name as DIR_NAME, " +
                "rf.xml_md5 as MD5, " +
                "rf.xml_enc_md5 as ENC_MD5," +
                "rf.xml_filetype as FILETYPE " +
                "FROM Run r, " +
                "XMLTABLE( '/RUN_SET/RUN/DATA_BLOCK/FILES/FILE' PASSING r.RUN_XML COLUMNS " +
                "xml_filename VARCHAR2(512) PATH '//@filename', " +
                "xml_filetype VARCHAR2(512) PATH '//@filetype', " +
                "xml_md5 VARCHAR2(512) PATH '//@unencrypted_checksum', " +
                "xml_enc_md5 VARCHAR2(512) PATH '//@checksum') rf, " +
                "EGA_SUBMISSION_ACCOUNT esa " +
                "WHERE r.audit_time > sysdate-" + days + " " +
                "AND esa.submission_account_id = r.EGA_SUBMISSION_ACCOUNT_ID " +
                "AND r.status_id=2";
        List<EgaAuditFile> files = eraproTemplate.query(query, (resultSet, i) -> {
            try {
                return createFile(resultSet);
            } catch (IllegalArgumentException e) {
                logger.warn("Skipping element {}", e.getMessage());
                return null;
            }
        });
        return files.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private EgaAuditFile createFile(ResultSet resultSet) throws SQLException {
        String egaId = resultSet.getString("EGA_ID");
        String filename = resultSet.getString("FILENAME");
        String fileType = resultSet.getString("FILETYPE");
        String box = resultSet.getString("DIR_NAME");
        String unencryptedMd5 = resultSet.getString("md5");
        String encryptedMd5 = resultSet.getString("enc_md5");
        return new EgaAuditFile(egaId, filename, fileType, box, unencryptedMd5, encryptedMd5);
    }

    public List<EgaAuditFile> getFilesLinkedToAnalysisInTheLastDays(long days) {
        String query = "SELECT " +
                "A.EGA_ID, " +
                "af.xml_filename as FILENAME, " +
                "af.xml_filetype as FILETYPE, " +
                "esa.dir_name as DIR_NAME, " +
                "af.xml_md5 as MD5, " +
                "af.xml_enc_md5 as ENC_MD5 " +
                "FROM analysis A, " +
                "XMLTABLE('/ANALYSIS_SET/ANALYSIS/FILES/FILE' PASSING A.ANALYSIS_XML COLUMNS " +
                "xml_filename VARCHAR2(512) PATH '//@filename', " +
                "xml_filetype VARCHAR2(512) PATH '//@filetype', " +
                "xml_md5 VARCHAR2(512) PATH '//@unencrypted_checksum', " +
                "xml_enc_md5 VARCHAR2(512) PATH '//@checksum') af, " +
                "EGA_SUBMISSION_ACCOUNT esa " +
                "WHERE a .audit_time > sysdate - " + days + " " +
                "AND esa.submission_account_id = A .EGA_SUBMISSION_ACCOUNT_ID " +
                "AND a .status_id = 2";
        List<EgaAuditFile> files = eraproTemplate.query(query, (resultSet, i) -> {
            try {
                return createFile(resultSet);
            } catch (IllegalArgumentException e) {
                logger.warn("Skipping element {}", e.getMessage());
                return null;
            }
        });
        return files.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
