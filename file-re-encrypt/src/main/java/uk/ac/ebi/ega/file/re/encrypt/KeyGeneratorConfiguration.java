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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.ac.ebi.ega.file.re.encrypt.services.key.ConstantKeyGenerator;
import uk.ac.ebi.ega.file.re.encrypt.services.IKeyGenerator;
import uk.ac.ebi.ega.file.re.encrypt.services.key.RandomKeyGenerator;

import java.io.IOException;

@Configuration
public class KeyGeneratorConfiguration {

    @Value("${file-re-encrypt.key.provider.constant.file}")
    private String constantFile;

    @Value("${file-re-encrypt.key.provider.random.size}")
    private int randomSize;

    @Bean
    @ConditionalOnProperty(value = "file-re-encrypt.key.provider.type", havingValue = "constant")
    public IKeyGenerator constantKeyGenerator() throws IOException {
        return new ConstantKeyGenerator(constantFile);
    }

    @Bean
    @ConditionalOnProperty(value = "file-re-encrypt.key.provider.type", havingValue = "random")
    public IKeyGenerator randomKeyGenerator() {
        return new RandomKeyGenerator(randomSize);
    }

}