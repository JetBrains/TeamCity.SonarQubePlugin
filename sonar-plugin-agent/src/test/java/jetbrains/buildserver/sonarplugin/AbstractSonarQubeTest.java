package jetbrains.buildserver.sonarplugin;

import okhttp3.tls.HeldCertificate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import static jetbrains.buildserver.sonarplugin.TestTools.writeTo;

public class AbstractSonarQubeTest {
    static final GenericContainer SONAR_QUBE_CONTAINER;
    static final GenericContainer NGINX_CONTAINER;
//    static final GenericContainer SONAR_SCANNER_CLI_CONTAINER;

    static {
        String localhost = null;
        File certificate = null;
        File privateKey = null;
        File nginxConf = null;
        File sampleFile;
        String networkAlias = "sonarqube";
        try {
            localhost = InetAddress.getByName("localhost").getCanonicalHostName();
            HeldCertificate localhostCertificate = new HeldCertificate.Builder()
                    .addSubjectAlternativeName(localhost)
                    .build();
            certificate = writeTo("certificate", localhostCertificate.certificatePem());
            privateKey = writeTo("key", localhostCertificate.privateKeyPkcs8Pem());
            nginxConf = writeTo("nginx", "" +
                    "user  nginx;\n" +
                    "worker_processes  auto;\n" +
                    "\n" +
                    "error_log  /var/log/nginx/error.log notice;\n" +
                    "pid        /var/run/nginx.pid;\n" +
                    "\n" +
                    "events {\n" +
                    "    worker_connections  1024;\n" +
                    "}\n" +
                    "" +
                    "http {\n" +
                    " include       /etc/nginx/mime.types;\n" +
                    " default_type  application/octet-stream;\n" +
                    " keepalive_timeout  65;\n" +
                    " sendfile        on;\n" +
                    "\n" +
                    " server {\n" +
                    "  listen 443;\n" +
                    "  ssl on;\n" +
                    "  ssl_certificate /etc/nginx/cert.pem;\n" +
                    "  ssl_certificate_key /etc/nginx/key.pem;\n" +
                    "  location / {\n" +
                    "     proxy_pass http://" + networkAlias + ":9000;\n" +
                    "  }\n" +
                    " }\n" +
                    "}\n");
            String sample = "fun vulnerableFunction() {\n" +
                    "  val password = \"password\" // Vulnerability - hardcoded password\n" +
                    "  if (!password.isNull()) println(\"null password!\")\n" +
                    "}\n" +
                    "\n" +
                    "// Code Smell - Empty function\n" +
                    "fun emptyFunction() {\n" +
                    "}\n" +
                    "\n" +
                    "fun buggyFunction(str: String){\n" +
                    "  if (str == \"hello\"){\n" +
                    "    println(\"Hello!\")\n" +
                    "  } else if (str == \"goodbye\"){\n" +
                    "    println(\"Goodbye!\")\n" +
                    "  } else if (str == \"hello\"){ // Bug - Duplicate condition\n" +
                    "    println(\"Hello again!\")\n" +
                    "  }\n" +
                    "}";
            sampleFile = writeTo("kotlin", sample, ".kt");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Network nw = Network.newNetwork();
        SONAR_QUBE_CONTAINER = new GenericContainer(DockerImageName.parse("sonarqube:9.1.0-developer"))
                .withNetwork(nw)
                .withNetworkAliases(networkAlias)
                .withExposedPorts(9000);
        SONAR_QUBE_CONTAINER.start();
        NGINX_CONTAINER = new GenericContainer(DockerImageName.parse("nginx:1.21.4-perl"))
                .withNetwork(nw)
                .withNetworkAliases("nginx")
                .withCopyFileToContainer(MountableFile.forHostPath(nginxConf.getPath()), "/etc/nginx/nginx.conf")
                .withCopyFileToContainer(MountableFile.forHostPath(certificate.getPath()), "/etc/nginx/cert.pem")
                .withCopyFileToContainer(MountableFile.forHostPath(privateKey.getPath()), "/etc/nginx/key.pem")
                .withExposedPorts(443)
        ;
        NGINX_CONTAINER.start();
//        NGINX_CONTAINER.setPortBindings(new ArrayList<String>(Collections.singleton("0.0.0.0:" + 8443 + ":" + 443)));
        URL res = AbstractSonarQubeTest.class.getResource("nginx");
//        NGINX_CONTAINER.withFileSystemBind();
//        NGINX_CONTAINER.start();
//        docker run \
//        --rm \
//        -e SONAR_HOST_URL="http://${SONARQUBE_URL}" \
//        -e SONAR_LOGIN="myAuthenticationToken" \
//        -v "${YOUR_REPO}:/usr/src" \

//        SONAR_SCANNER_CLI_CONTAINER = new GenericContainer(DockerImageName.parse("sonarsource/sonar-scanner-cli")).
    }

}
