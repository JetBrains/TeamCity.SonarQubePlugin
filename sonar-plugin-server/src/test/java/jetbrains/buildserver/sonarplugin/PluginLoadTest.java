

package jetbrains.buildserver.sonarplugin;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PluginLoadTest {

    @Test
    public void testServerSpringLoad() {
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("/META-INF/build-server-plugin-sonar-plugin.xml")) {
            // TeamCity Spring server context is not loaded => will fail due to dependencies
        } catch (Exception e) {
            Assert.assertTrue(
                    e.getMessage().contains("No qualifying bean of type 'jetbrains.buildServer.web.openapi.WebControllerManager' available"),
                    "Error message must say that WebControllerManager is not available");
        }
    }
   
}