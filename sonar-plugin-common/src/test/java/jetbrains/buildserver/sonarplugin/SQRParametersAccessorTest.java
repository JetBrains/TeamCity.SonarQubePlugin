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
