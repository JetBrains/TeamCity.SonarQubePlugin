package jetbrains.buildserver.sonarplugin.msbuild.tool;

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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

@Test(dataProvider = "getFileSystemAndRoot", dataProviderClass = DataProviders.class)
public class SimpleZipToolProviderSQMSBuildTest {
    private static final String CORRECT_PLUGIN_NAME = "sonar-scanner-msbuild-1.2.3.zip";
    private static final String SOME_EXE = "Some.exe";
    private SimpleZipToolProviderSQMSBuild myToolProvider;

    @BeforeMethod
    public void setUp() {
        final Mockery mockery = new Mockery();
        myToolProvider = new SimpleZipToolProviderSQMSBuild(mockery.mock(PluginDescriptor.class), new SonarQubeMSBuildToolType());
    }

    public void testValidatePackedTool(@NotNull final FileSystem jimfs, @NotNull final String root) throws IOException, ToolException {
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild.1.2.3.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-1.2.3.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-1.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-1.2.3.4.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-5.1.0.28487-net5.0.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-5.1.0.28487-netcoreapp2.0.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-5.1.0.28487-net46.zip", root, "some", "path"));
        myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-9.0.0.100868-net-framework.zip", root, "some", "path"));

        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuil-1.2.3.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-1.2.3a.zip", root, "some", "path"))).isInstanceOf(ToolException.class);
        assertThatThrownBy(() -> myToolProvider.validatePackedTool(prepareCorrectPluginWithName(jimfs, "sonar-scanner-msbuild-1.2.3.ip", root, "some", "path"))).isInstanceOf(ToolException.class);
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
        then(res.getDetails()).doesNotContain(SimpleZipToolProviderSQMSBuild.BIN).contains(SimpleZipToolProviderSQMSBuild.SONAR_QUBE_SCANNER_MSBUILD_EXE);

        zip = TestTools.prepareZip(getPluginPath(jimfs, root, "some", "path"), CORRECT_PLUGIN_NAME, zfs -> {
            Files.createDirectory(zfs.getPath(SimpleZipToolProviderSQMSBuild.BIN));
        });

        res = myToolProvider.parseVersion(zip, "1.2.3");
        then(res.getToolVersion()).isNull();
        then(res.getDetails()).doesNotContain(SimpleZipToolProviderSQMSBuild.BIN).contains(SimpleZipToolProviderSQMSBuild.SONAR_QUBE_SCANNER_MSBUILD_EXE);
    }

    public void testLayoutContents(@NotNull final FileSystem jimfs, @NotNull final String root) throws Exception {
        Path zip = prepareCorrectPlugin(jimfs, root, "some", "path");

        Path target = jimfs.getPath(root, "target");
        myToolProvider.layoutContents(zip, target);

        Path xml = target.resolve(SimpleZipToolProviderSQMSBuild.TEAMCITY_PLUGIN_XML);
        then(Files.exists(xml)).describedAs(SimpleZipToolProviderSQMSBuild.TEAMCITY_PLUGIN_XML + " should have been created").isTrue();
        List<String> lines = Files.readAllLines(xml);
        then(lines).extracting(s -> s.trim()).contains("<executable-files>", "</executable-files>", "<include name='" + SimpleZipToolProviderSQMSBuild.SONAR_QUBE_SCANNER_MSBUILD_EXE + "'/>");

        boolean found = false;
        for (String line : lines) {
            if (line.contains("include name=") && line.contains(SimpleZipToolProviderSQMSBuild.SONAR_SCANNER_PREFIX) && line.contains(SOME_EXE)) {
                found = true;
                break;
            }
        }
        if (!found) {
            fail("Didn't find <include name='sonar-scanner\\bin\\Some.exe'/>");
        }
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
            Files.createFile(zfs.getPath("/", SimpleZipToolProviderSQMSBuild.SONAR_QUBE_SCANNER_MSBUILD_EXE));
            Files.createDirectory(zfs.getPath("/", SimpleZipToolProviderSQMSBuild.BIN));
            Files.createDirectories(zfs.getPath("/", SimpleZipToolProviderSQMSBuild.SONAR_SCANNER_PREFIX, SimpleZipToolProviderSQMSBuild.BIN));
            Files.createFile(zfs.getPath("/", SimpleZipToolProviderSQMSBuild.SONAR_SCANNER_PREFIX, SimpleZipToolProviderSQMSBuild.BIN, SOME_EXE));
        });
    }
}
