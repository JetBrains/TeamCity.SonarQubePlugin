package jetbrains.buildserver.sonarplugin.sqrunner.tool;

import jetbrains.buildServer.tools.GetPackageVersionResult;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildserver.sonarplugin.DataProviders;
import jetbrains.buildserver.sonarplugin.TestTools;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.BDDAssertions.then;

@Test(dataProvider = "getFileSystemAndRoot", dataProviderClass = DataProviders.class)
public class SimpleZipToolProviderSQScannerTest {
    private static final String CORRECT_PLUGIN_NAME = "sonar-qube-scanner-1.2.3-scanner.jar";
    private SimpleZipToolProviderSQScanner myToolProvider;

    @BeforeMethod
    public void setUp() {
        final Mockery mockery = new Mockery();
        myToolProvider = new SimpleZipToolProviderSQScanner(mockery.mock(PluginDescriptor.class), new SonarQubeScannerToolType());
    }

    public void testValidatePackedTool(@NotNull final FileSystem jimfs, @NotNull final String root) throws IOException, ToolException {
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner.1.2.3-scanner.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-1.2.3-scanner.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-1-scanner.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-1.2.3.4-scanner.zip", root, "some", "path"));

        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-scanner.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanne-1.2.3-scanner.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-1.2.3a-scanner.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-qube-scanner-1.2.3.ip", root, "some", "path"))).isInstanceOf(ToolException.class);
    }

    public void testParseVersion(@NotNull final FileSystem jimfs, @NotNull final String root) throws Exception {
        Path zip = prepareCorrectPlugin(jimfs, root, "some", "path");

        GetPackageVersionResult res = myToolProvider.parseVersion(zip, "1.2.3");
        then(res.getToolVersion()).describedAs("Should be correct plugin but was '" + res.getDetails() + "'").isNotNull();
        then(res.getToolVersion().getVersion()).isEqualTo("1.2.3");
    }

    public void testParseVersionIncorrect(@NotNull final FileSystem jimfs, @NotNull final String root) throws Exception {
        Path zip = prepareIncorrectPlugin(jimfs, root, "some", "path");

        GetPackageVersionResult res = myToolProvider.parseVersion(zip, "1.2.3");
        then(res.getToolVersion()).isNull();
    }

    public void testLayoutContents(@NotNull final FileSystem jimfs, @NotNull final String root) throws Exception {
        Path zip = prepareCorrectPlugin(jimfs, root, "some", "path");

        Path target = jimfs.getPath(root, "target");
        myToolProvider.layoutContents(zip, target);

        Path lib = target.resolve(SimpleZipToolProviderSQScanner.LIB);
        then(Files.exists(lib)).isTrue();
        then(Files.list(lib).count()).isEqualTo(1L);
        then(Files.list(lib).findAny().get().getFileName().toString()).isEqualTo(CORRECT_PLUGIN_NAME);
    }

    private Path prepareIncorrectPlugin(@NotNull final FileSystem fs,
                                        @NotNull final String root,
                                        @NotNull final String... path) throws IOException {
        Path plugin = getPluginPath(fs, root, path);
        return TestTools.prepareZip(plugin, CORRECT_PLUGIN_NAME, zfs -> {
        });
    }

    private Path getPluginPath(@NotNull final FileSystem fs, @NotNull final String root, @NotNull final String... path) {
        return fs.getPath(root, path);
    }

    private Path prepareCorrectPlugin(@NotNull final FileSystem fs,
                                      @NotNull final String root,
                                      @NotNull final String... path) throws IOException {
        return prepareCorrectPluginWithName(fs, CORRECT_PLUGIN_NAME, root, path);
    }

    private Path prepareCorrectPluginWithName(@NotNull final FileSystem fs,
                                              @NotNull final String packageName,
                                              @NotNull final String root,
                                              @NotNull final String... path) throws IOException {
        Path plugin = getPluginPath(fs, root, path);
        return TestTools.prepareZip(plugin, packageName, zfs -> {
            Files.createDirectories(zfs.getPath("/", SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION).getParent());
            Files.createFile(zfs.getPath("/", SimpleZipToolProviderSQScanner.SCANNER_MAIN_CLASS_LOCATION));
        });
    }
}