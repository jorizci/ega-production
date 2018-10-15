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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.io.File;
import java.sql.Date;
import java.util.Calendar;

public class ProFilerService {

    private final NamedParameterJdbcTemplate proFilerTemplate;

    public ProFilerService(NamedParameterJdbcTemplate proFilerTemplate) {
        this.proFilerTemplate = proFilerTemplate;
    }

    public Number insertFile(String egaFileId, File file, String md5) {
        String query = "INSERT INTO re_file(" +
                "name," +
                "md5," +
                "type," +
                "size," +
                "host_id," +
                "created," +
                "updated," +
                "ega_file_stable_id" +
                ") " +
                "VALUES(" +
                ":name," +
                ":md5," +
                ":type," +
                ":size," +
                ":host_id," +
                ":created," +
                ":updated," +
                ":ega_id)";
        Date date = getCurrentDate();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", file.getName());
        parameters.addValue("md5", md5);
        parameters.addValue("type", "GPG_ENCRYPTED");
        parameters.addValue("size", file.length());
        parameters.addValue("host_id", 1);
        parameters.addValue("created", date);
        parameters.addValue("updated", date);
        parameters.addValue("ega_id", egaFileId);

        KeyHolder holder = new GeneratedKeyHolder();
        proFilerTemplate.update(query, parameters, holder);
        return holder.getKey();
    }

    public void insertArchive(Number fileId, String relativePath, File file, String md5) {
        String query = "INSERT INTO archive(" +
                "name," +
                "file_id," +
                "md5," +
                "size," +
                "relative_path," +
                "volume_name," +
                "priority," +
                "created," +
                "updated," +
                "archive_action_id," +
                "archive_location_id" +
                ") " +
                "VALUES(" +
                ":name," +
                ":file_id," +
                ":md5," +
                ":size," +
                ":relative_path," +
                ":volume_name," +
                ":priority," +
                ":created," +
                ":updated," +
                ":archive_action_id," +
                ":archive_location_id)";
        Date date = getCurrentDate();
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("name", file.getName());
        parameters.addValue("file_id", fileId);
        parameters.addValue("md5", md5);
        parameters.addValue("size", file.length());
        parameters.addValue("relative_path", relativePath);
        parameters.addValue("volume_name", "vol1");
        parameters.addValue("priority", "50");
        parameters.addValue("created", date);
        parameters.addValue("updated", date);
        parameters.addValue("archive_action_id", 1);
        parameters.addValue("archive_location_id", 1);
        proFilerTemplate.update(query, parameters);
    }

    private Date getCurrentDate() {
        return new Date(Calendar.getInstance().toInstant().toEpochMilli());
    }
}
