package jetbrains.buildserver.sonarplugin;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by Andrey Titov on 6/24/14.
 */
public class SQRBuildServiceTest {
    @Test
    public void testAddSQRArg() {
        final List<String> res = new LinkedList<String>();
        SQRBuildService.addSQRArg(res, "-Dkey", null);
        assertTrue(res.isEmpty(), "Null values should not be passed to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value");
        assertTrue(!res.isEmpty(), "'-Dkey=value' pair should be added to the resulting list. Nothing was added.");
        assertEquals(res.get(0), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 1, "Only '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));

        SQRBuildService.addSQRArg(res, "-Dkey", "value");
        assertEquals(res.get(0), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertEquals(res.get(1), "-Dkey=value", "'-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
        assertTrue(res.size() == 2, "Second '-Dkey=value' pair should be added to the resulting list. List: " + Arrays.toString(res.toArray()));
    }
}
