package jetbrains.buildserver.sonarplugin;

import jetbrains.buildserver.sonarplugin.util.SSLTools;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static jetbrains.buildserver.sonarplugin.TestTools.createTempDirectory;
import static jetbrains.buildserver.sonarplugin.TestTools.writeTo;
import static org.assertj.core.api.Assertions.assertThat;


public class SSLToolsTest {
    @Test
    public void addOneCertificateToCacerts() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        String certificateContent = "-----BEGIN CERTIFICATE-----\n" +
                "MIIBaTCCAQ6gAwIBAgIBATAKBggqhkjOPQQDAjAvMS0wKwYDVQQDDCQ0NTJiNmMx\n" +
                "OS1lYjY4LTRlNzAtYjE3Ny0zZTU1M2U1YzZkMzYwHhcNMjExMTEyMjAwMDQ4WhcN\n" +
                "MjExMTEzMjAwMDQ4WjAvMS0wKwYDVQQDDCQ0NTJiNmMxOS1lYjY4LTRlNzAtYjE3\n" +
                "Ny0zZTU1M2U1YzZkMzYwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAStwNVdUQln\n" +
                "31aEA7C8pLHZqFF7eVrjruZSxlLiFGaszL5ht0IwP1C2xpV4zqn8W4if/FSjPUr9\n" +
                "cG+gjucetp1noxswGTAXBgNVHREBAf8EDTALgglsb2NhbGhvc3QwCgYIKoZIzj0E\n" +
                "AwIDSQAwRgIhANhI7Uq9V/l1bnNKunOhbYG9Yi6Td2abEZaI700gKUhqAiEA1prc\n" +
                "5nDPIDtcYOgQnxkPA0mrZ57oRPnjJ8/KDUyb6aE=\n" +
                "-----END CERTIFICATE-----\n";
        Path tmpDir = createTempDirectory();
        File certificateFile = writeTo("certificate", certificateContent, ".cer", tmpDir.toFile());
        Path pathToPatchedCacerts = SSLTools.cloneKeyStoreWithTC(null, tmpDir.toString());
        assertThat(pathToPatchedCacerts).isNotNull();
        try (FileInputStream fis = new FileInputStream(pathToPatchedCacerts.toFile())) {
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(fis, "changeit".toCharArray());
        }
    }
}
