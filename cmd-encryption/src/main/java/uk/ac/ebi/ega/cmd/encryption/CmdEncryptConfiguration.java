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
package uk.ac.ebi.ega.cmd.encryption;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.cmd.encryption.options.CmdEncryptOptions;
import uk.ac.ebi.ega.cmd.encryption.properties.FireProperties;
import uk.ac.ebi.ega.cmd.encryption.services.FileReEncryptService;
import uk.ac.ebi.ega.cmd.encryption.services.fire.FireService;

import java.util.Optional;

@Configuration
public class CmdEncryptConfiguration {

    @Bean
    public CommandLineRunner clr() {
        return args -> {
            Optional<CmdEncryptOptions> var = CmdEncryptOptions.parse(args);
            if (var.isPresent()) {
                CmdEncryptOptions options = var.get();
                fileReEncryptService().reEncryptFile(
                        options.getFireFilePath(),
                        options.getOutputPath(),
                        options.getOutputFormat(),
                        options.isUseFireMount(),
                        options.getRetries(),
                        options.getPasswordFile(),
                        options.getOutputPasswordFile()
                );
            } else {
                System.exit(1);
            }
        };
    }

    @Bean
    public FileReEncryptService fileReEncryptService() {
        return new FileReEncryptService(fireService());
    }

    @Bean
    @ConfigurationProperties("cmd-re-encrypt.fire")
    public FireProperties fireProperties() {
        return new FireProperties();
    }

    @Bean
    public FireService fireService() {
        return new FireService(fireProperties());
    }

}
