/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                    e.getMessage());
        }
    }
   
}
