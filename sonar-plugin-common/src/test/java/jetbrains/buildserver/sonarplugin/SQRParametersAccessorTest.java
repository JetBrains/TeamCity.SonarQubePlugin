package jetbrains.buildserver.sonarplugin;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

/**
 * Created by Andrey Titov on 4/8/14.
 */
public class SQRParametersAccessorTest {
    @Test
    public void testGetProjectName() {
        final SQRParametersAccessor accessor = new SQRParametersAccessor(Collections.singletonMap(Constants.SONAR_PROJECT_KEY, "key"));
        Assert.assertEquals(accessor.getProjectName(), null, "Should be 'key'");
    }
}
