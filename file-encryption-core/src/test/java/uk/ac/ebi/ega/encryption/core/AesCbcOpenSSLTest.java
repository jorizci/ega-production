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

import org.bouncycastle.util.io.Streams;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.ega.encryption.core.encryption.AesCbcOpenSSL;

import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class AesCbcOpenSSLTest {

    @Test
    public void testDecrypt() throws IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeyException {
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("file.enc");
        AesCbcOpenSSL aesCbcOpenSSL = new AesCbcOpenSSL();
        byte[] test = Streams.readAll(aesCbcOpenSSL.decrypt(input, "kiwi".toCharArray()));
        assertEquals("test file.\n", new String(test));
    }

    @Test
    public void testEncrypt() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        AesCbcOpenSSL aesCbcOpenSSL = new AesCbcOpenSSL();
        aesCbcOpenSSL.setFixedSalt(DatatypeConverter.parseHexBinary("B392A696565D0728"));
        final OutputStream encrypt = aesCbcOpenSSL.encrypt("kiwi".toCharArray(), baos);

        encrypt.write("test file.\n".getBytes(UTF_8));
        encrypt.close();

        final byte[] bytes = baos.toByteArray();
        Assert.assertEquals("da20deddddc00a3d267760b718b72a25",
                DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(bytes)).toLowerCase());
    }

}
