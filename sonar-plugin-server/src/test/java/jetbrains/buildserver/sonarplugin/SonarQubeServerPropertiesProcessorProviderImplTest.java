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

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.BDDAssertions.then;

public class SonarQubeServerPropertiesProcessorProviderImplTest {
    @Test
    public void testProcessor() {
        final PropertiesProcessor processor = new SonarQubeServerPropertiesProcessorProviderImpl().getRunnerPropertiesProcessor();

        {
            final Collection<InvalidProperty> process = processor.process(Collections.emptyMap());
            then(process).hasSize(1);
            then(process.iterator().next().getPropertyName()).isEqualTo(Constants.SQS_CHOOSER);
        }

        {
            final Collection<InvalidProperty> process = processor.process(Collections.singletonMap(Constants.SONAR_SERVER_ID, ""));
            then(process).isEmpty();
        }
    }
}