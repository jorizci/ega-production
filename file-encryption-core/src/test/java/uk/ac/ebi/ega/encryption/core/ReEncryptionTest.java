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
package uk.ac.ebi.ega.encryption.core;

import org.bouncycastle.openpgp.PGPException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.ac.ebi.ega.encryption.core.exception.OutputFileAlreadyExists;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.assertEquals;


public class ReEncryptionTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testCipEncryption() throws URISyntaxException, IOException, PGPException, NoSuchAlgorithmException,
            OutputFileAlreadyExists, InvalidAlgorithmParameterException, InvalidKeySpecException, InvalidKeyException {
        File file = new File(this.getClass().getClassLoader().getResource("test.txt.gpg").toURI());
        File outputFile = temporaryFolder.newFile();

        final ReEncryptionReport report = ReEncryption.reEncrypt(file, "test".toCharArray(), outputFile,
                "test2".toCharArray(), true);

        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream is = Files.newInputStream(outputFile.toPath());
             DigestInputStream dis = new DigestInputStream(is, md)) {
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1) {
            }
        }
        assertEquals("4221d002ceb5d3c9e9137e495ceaa647", report.getUnencryptedMd5());
        assertEquals(report.getReEncryptedMd5(), DatatypeConverter.printHexBinary(md.digest()).toLowerCase());
    }
}
