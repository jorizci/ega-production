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
import org.bouncycastle.util.io.Streams;
import org.junit.Test;
import uk.ac.ebi.ega.encryption.core.encryption.PgpSymmetric;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;

public class PgpSymmetricTest {

    @Test
    public void testDecryptFile() throws IOException, PGPException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("test.txt.gpg");
        PgpSymmetric pgpSymmetric = new PgpSymmetric();
        byte[] test = Streams.readAll(pgpSymmetric.decrypt(input, "test".toCharArray()));
        assertEquals("this is a test file\n", new String(test));
    }

    @Test(expected = IOException.class)
    public void testDecryptFileWrongPassword() throws IOException, PGPException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("test.txt.gpg");
        PgpSymmetric pgpSymmetric = new PgpSymmetric();
        byte[] test = Streams.readAll(pgpSymmetric.decrypt(input, "kiwi".toCharArray()));
    }

    @Test(expected = IOException.class)
    public void testDecryptFileBadFile() throws IOException, PGPException {
        InputStream input = new ByteArrayInputStream("This is not a pgp stream".getBytes());
        PgpSymmetric pgpSymmetric = new PgpSymmetric();
        byte[] test = Streams.readAll(pgpSymmetric.decrypt(input, "kiwi".toCharArray()));
    }
}
