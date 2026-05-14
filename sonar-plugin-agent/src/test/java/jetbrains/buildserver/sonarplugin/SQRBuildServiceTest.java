

package jetbrains.buildserver.sonarplugin;

import jetbrains.buildServer.util.OSType;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by Andrey Titov on 6/24/14.
 */
public class SQRBuildServiceTest {
    @Test
    public void testAddSQRArg() {
        final List<String> res = new LinkedList<String>();
        SQRBuildService.addSQRArg(res, "-Dkey", null, OSType.UNIX);
        assertTrue(res.isEmpty(), "Null values should not be passed to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value", OSType.UNIX);
        assertTrue(!res.isEmpty(), "'-Dkey=value' pair should be added to the resulting list. Nothing was added.");
        assertEquals(res.get(0), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 1, "Only '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value", OSType.UNIX);
        assertEquals(res.get(0), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertEquals(res.get(1), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 2, "Second '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
    }

    @Test
    public void testAddSQRArgWin() {
        final List<String> res = new LinkedList<String>();
        SQRBuildService.addSQRArg(res, "-Dkey", null, OSType.WINDOWS);
        assertTrue(res.isEmpty(), "Null values should not be passed to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value", OSType.WINDOWS);
        assertTrue(!res.isEmpty(), "'-Dkey=value' pair should be added to the resulting list. Nothing was added.");
        assertEquals(res.get(0), "\"-Dkey=value\"", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 1, "Only '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value", OSType.WINDOWS);
        assertEquals(res.get(0), "\"-Dkey=value\"", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertEquals(res.get(1), "\"-Dkey=value\"", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 2, "Second '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
    }

    @Test
    public void testAddSQRArgWinEscapesTrailingBackslash() {
        final List<String> res = new LinkedList<String>();

        SQRBuildService.addSQRArg(res, "-Dkey", ".\\", OSType.WINDOWS);

        assertEquals(res.get(0), "\"-Dkey=.\\\\\"", "Trailing backslash should not escape the closing quote");
    }

    @Test
    public void testScannerArgsComposerEscapesWindowsSourcesTrailingBackslash() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(Constants.SONAR_PROJECT_SOURCES, ".\\");
        parameters.put(Constants.SONAR_PROJECT_TESTS, "src/test/java/example");

        final List<String> args = new SQScannerArgsComposer(OSType.WINDOWS).composeArgs(
                new SQRParametersAccessor(parameters),
                new JavaSonarQubeKeysProvider(),
                new HashMap<String, String>()
        );

        then(args).contains(
                "\"-Dsonar.sources=.\\\\\"",
                "\"-Dsonar.tests=src/test/java/example\""
        );
    }

    @Test
    public void test_project_key_escaping() {
        then(SQRBuildService.getProjectKey("")).isEqualTo("");
        then(SQRBuildService.getProjectKey("abc")).isEqualTo("abc");
        then(SQRBuildService.getProjectKey("123")).isEqualTo("123");
        then(SQRBuildService.getProjectKey("a1_-:.")).isEqualTo("a1_-:.");
        then(SQRBuildService.getProjectKey("a?a")).isEqualTo("a_a");
        then(SQRBuildService.getProjectKey("a?!#$%^&*()a")).isEqualTo("a__________a");
    }
}
