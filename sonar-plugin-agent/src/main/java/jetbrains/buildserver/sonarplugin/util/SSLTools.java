package jetbrains.buildserver.sonarplugin.util;

import jetbrains.buildserver.sonarplugin.SQScannerArgsComposer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public final class SSLTools {
    private static final Logger log = LoggerFactory.getLogger(SQScannerArgsComposer.class);

    /**
     * Returns path to temporary created keystore containing TC-trusted certificates with jdk-trusted ones.
     *
     * @param javaHome if null, this jvm keystore will be used
     * @param teamcityCertificateFolderPath path to folder containing TC-trusted certificates
     * @return path to created keystore
     */
    public static Path cloneKeyStoreWithTC(@Nullable String javaHome, String teamcityCertificateFolderPath) {
        try {
            KeyStore cacerts = getCacertsKeyStore(javaHome);
            File f = File.createTempFile("tmpks", ".jks");
            if (storeCertificates(cacerts, teamcityCertificateFolderPath) > 0) {
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    cacerts.store(fos, "changeit".toCharArray());
                }
                return f.toPath();
            }
            log.error("Error while using certificates from {}", teamcityCertificateFolderPath);
        } catch (Exception e) {
            log.error("Error while using certificates from {}", teamcityCertificateFolderPath, e);
        }
        return null;
    }

    public static KeyStore getCacertsKeyStore(String javaHome) throws Exception {
        File file = new File(getCacerts(javaHome));
        if (!file.exists()) {
            return null;
        }
        KeyStore instance = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = new FileInputStream(file)) {
            instance.load(in, "changeit".toCharArray());
            return instance;
        }
    }

    public static String getCacerts(String javaHome) {
        String sep = File.separator;
        if (javaHome == null)
            javaHome = System.getProperty("java.home");
        return javaHome + sep
                + "lib" + sep + "security" + sep
                + "cacerts";
    }

    private static int storeCertificates(KeyStore ks, String syncCertPath) {
        Path certificateFolder = new File(syncCertPath).toPath();
        AtomicInteger counter = new AtomicInteger(0);
        try {
            Files.walk(certificateFolder, 1)
                    .filter(Files::isRegularFile)
                    .map(it -> storeCertificate(ks, it))
                    .filter(Objects::nonNull)
                    .forEach(t -> counter.incrementAndGet());
        } catch (IOException e) {
            log.error("Error while copying certificates from {}", syncCertPath, e);
        }
        return counter.get();
    }

    private static Path storeCertificate(KeyStore ks, Path certificatePath) {
        try (FileInputStream fis = new FileInputStream(certificatePath.toFile())) {
            Certificate c = CertificateFactory.getInstance("X.509").generateCertificate(fis);
            ks.setCertificateEntry(certificatePath.toFile().getName(), c);
            return certificatePath;
        } catch (IOException | CertificateException | KeyStoreException e) {
            log.error("Error while reading certificate from {}", certificatePath, e);
        }
        return null;
    }

}
