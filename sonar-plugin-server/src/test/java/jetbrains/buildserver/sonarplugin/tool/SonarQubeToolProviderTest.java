package jetbrains.buildserver.sonarplugin.tool;

import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.DataProviders;
import jetbrains.buildserver.sonarplugin.TestTools;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SimpleZipToolProviderSQScanner;
import jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerToolType;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.BDDAssertions.then;

@Test(dataProvider = "getFileSystemAndRoot", dataProviderClass = DataProviders.class)
public class SonarQubeToolProviderTest {

    private SimpleZipToolProviderSQScanner myToolProvider;

    @BeforeMethod
    public void setUp(@NotNull final Method method, @NotNull final Object[] testData) {
        final Mockery mockery = new Mockery();
        myToolProvider = new SimpleZipToolProviderSQScanner(mockery.mock(PluginDescriptor.class), new SonarQubeScannerToolType());
    }

    public void testTryGetPackageVersion(@NotNull final FileSystem fs, @NotNull final String root) throws IOException {
        final SonarQubeToolProvider sonarQubeToolProvider = new SonarQubeToolProvider(myToolProvider);

        // correct name, scanner class
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner.3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-5.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "aaa-5.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();

        // incorrect name, scanner class
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "5.2.0.1227.zip", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "aaa.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNull();

        // correct name, runner class
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner.3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-5.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "aaa-5.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNotNull();

        // incorrect name, runner class
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "5.2.0.1227.zip", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "aaa.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION)))).getToolVersion()).isNull();

        // correct name, no class
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner.3.2.0.1227.jar", zipFS -> {})).getToolVersion()).isNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-3.2.0.1227.jar", zipFS -> {})).getToolVersion()).isNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner-5.2.0.1227.jar", zipFS -> {})).getToolVersion()).isNull();
        then(sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "aaa-5.2.0.1227.jar", zipFS -> {})).getToolVersion()).isNull();
    }

    public void testTryGetPackageVersion_shouldReturnCorrectToolId(@NotNull final FileSystem fs, @NotNull final String root) throws IOException {
        final SonarQubeToolProvider sonarQubeToolProvider = new SonarQubeToolProvider(myToolProvider);

        // correct name, scanner class
        final GetPackageVersionResult scanner = sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner.3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION))));
        then(scanner.getToolVersion()).isNotNull(); assert scanner.getToolVersion() != null;
        then(scanner.getToolVersion().getId()).isEqualTo("sonar-qube-scanner.3.2.0.1227-scanner");

        // incorrect name, runner class
        final GetPackageVersionResult runner = sonarQubeToolProvider.tryGetPackageVersion(TestTools.prepareZip(fs.getPath(root), "sonar-scanner.3.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION))));
        then(runner.getToolVersion()).isNotNull(); assert runner.getToolVersion() != null;
        then(runner.getToolVersion().getId()).isEqualTo("sonar-qube-scanner.3.2.0.1227-runner");
    }

    public void testUnpackToolPackage(@NotNull final FileSystem fs, @NotNull final String root) throws IOException, ToolException {
        final SonarQubeToolProvider sonarQubeToolProvider = new SonarQubeToolProvider(myToolProvider);

        final Path targetPath = fs.getPath(root, "sonar-qube-scanner.3.2.0.1227-scanner");
        sonarQubeToolProvider.unpackToolPackage(
                TestTools.prepareZip(fs.getPath(root), "aaa-5.2.0.1227.jar", zipFS -> TestTools.createFile(TestTools.fromZipString(zipFS, SimpleZipToolProviderSQScanner.RUNNER_MAIN_CLASS_LOCATION))),
                targetPath
        );

        then(Files.exists(targetPath.resolve("lib"))).isTrue();
        then(Files.exists(targetPath.resolve("lib").resolve("aaa-5.2.0.1227.jar"))).isTrue();
    }
}