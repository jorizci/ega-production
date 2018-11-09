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
package uk.ac.ebi.ega.file.re.encrypt;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfiguration {

    @Bean("erapro_datasource_properties")
    @ConfigurationProperties("file-re-encrypt.datasource.erapro")
    public DataSourceProperties eraproDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("erapro_datasource")
    public DataSource eraproDataSource() {
        return eraproDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("erapro_jdbc_template")
    public JdbcTemplate eraproJdbcTemplate() {
        return new JdbcTemplate(eraproDataSource());
    }

    @Bean("erapro_transaction_manager")
    public DataSourceTransactionManager eraproTransactionManager() {
        return new DataSourceTransactionManager(eraproDataSource());
    }

    @Bean("audit_datasource_properties")
    @ConfigurationProperties("file-re-encrypt.datasource.audit")
    public DataSourceProperties auditDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("audit_datasource")
    public DataSource auditDataSource() {
        return auditDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("audit_jdbc_template")
    public NamedParameterJdbcTemplate auditJdbcTemplate() {
        return new NamedParameterJdbcTemplate(auditDataSource());
    }

    @Bean("audit_transaction_manager")
    public DataSourceTransactionManager auditTransactionManager() {
        return new DataSourceTransactionManager(auditDataSource());
    }

    @Bean("re_encrypt_datasource_properties")
    @ConfigurationProperties("file-re-encrypt.datasource.re-encrypt")
    public DataSourceProperties reEncryptDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("re_encrypt_datasource")
    public DataSource reEncryptDataSource() {
        return reEncryptDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("re_encrypt_jdbc_template")
    public NamedParameterJdbcTemplate reEncryptJdbcTemplate() {
        return new NamedParameterJdbcTemplate(reEncryptDataSource());
    }

    @Bean("re_encrypt_transaction_manager")
    public DataSourceTransactionManager reEncryptTransactionManager() {
        return new DataSourceTransactionManager(reEncryptDataSource());
    }

    @Bean("pro_filer_datasource_properties")
    @ConfigurationProperties("file-re-encrypt.datasource.pro-filer")
    public DataSourceProperties proFilerDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("pro_filer_datasource")
    public DataSource proFilerDataSource() {
        return proFilerDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("pro_filer_jdbc_template")
    public NamedParameterJdbcTemplate proFilerJdbcTemplate() {
        return new NamedParameterJdbcTemplate(proFilerDataSource());
    }

    @Bean("pro_filer_transaction_manager")
    public DataSourceTransactionManager proFilerTransactionManager() {
        return new DataSourceTransactionManager(proFilerDataSource());
    }

    @Bean("pea_datasource_properties")
    @ConfigurationProperties("file-re-encrypt.datasource.pea")
    public DataSourceProperties peaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("pea_datasource")
    public DataSource peaDataSource() {
        return peaDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean("pea_jdbc_template")
    public NamedParameterJdbcTemplate peaJdbcTemplate() {
        return new NamedParameterJdbcTemplate(peaDataSource());
    }

    @Bean("pea_transaction_manager")
    public DataSourceTransactionManager peaTransactionManager() {
        return new DataSourceTransactionManager(peaDataSource());
    }

}
