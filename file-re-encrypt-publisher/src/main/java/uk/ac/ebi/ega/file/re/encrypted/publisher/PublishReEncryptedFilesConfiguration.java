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
package uk.ac.ebi.ega.file.re.encrypted.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.ac.ebi.ega.database.commons.services.AuditService;
import uk.ac.ebi.ega.database.commons.services.EraProService;
import uk.ac.ebi.ega.database.commons.services.ProFilerService;
import uk.ac.ebi.ega.database.commons.services.ReEncryptService;
import uk.ac.ebi.ega.file.re.encrypted.publisher.services.PublishReEncryptedFilesService;

import java.util.Optional;

@Configuration
public class PublishReEncryptedFilesConfiguration {

    private final Logger logger = LoggerFactory.getLogger(PublishReEncryptedFilesConfiguration.class);

    @Autowired
    @Qualifier("erapro_jdbc_template")
    private JdbcTemplate eraproTemplate;

    @Autowired
    @Qualifier("audit_jdbc_template")
    private NamedParameterJdbcTemplate auditTemplate;

    @Autowired
    @Qualifier("re_encrypt_jdbc_template")
    private NamedParameterJdbcTemplate reEncryptTemplate;

    @Autowired
    @Qualifier("pro_filer_jdbc_template")
    private NamedParameterJdbcTemplate proFilerTemplate;

    @Bean
    public CommandLineRunner clr() {
        return args -> {
            Optional<PublishReEncryptedFilesOptions> var = PublishReEncryptedFilesOptions.parse(args);
            if (var.isPresent()) {
                PublishReEncryptedFilesOptions options = var.get();
                publishReEncryptedFilesService().publish(options.getEgaId());
            } else {
                System.exit(1);
            }
        };
    }

    @Bean
    public PublishReEncryptedFilesService publishReEncryptedFilesService() {
        return new PublishReEncryptedFilesService(auditService(), reEncryptService());
    }

    @Bean
    public AuditService auditService() {
        return new AuditService(auditTemplate);
    }

    @Bean
    public EraProService eraProService() {
        return new EraProService(eraproTemplate);
    }

    @Bean
    public ReEncryptService reEncryptService() {
        return new ReEncryptService(reEncryptTemplate);
    }

    @Bean
    public ProFilerService proFilerService() {
        return new ProFilerService(proFilerTemplate);
    }

}
