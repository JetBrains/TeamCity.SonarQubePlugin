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

package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildserver.sonarplugin.manager.projectsettings.XMLBasedSQSInfoHelper;
import jetbrains.buildserver.sonarplugin.manager.projectsettings.SQSProjectSettings;
import org.assertj.core.api.BDDAssertions;
import org.testng.annotations.Test;

/**
 * Created by linfar on 03.10.16.
 */
@Test
public class SQSProjectSettingsTest {
    public void test() {
        final SQSProjectSettings holder = new SQSProjectSettings();
        BDDAssertions.then(holder.getInfo("serverId")).isNull();

        holder.setInfo("serverId", XMLBasedSQSInfoHelper.createServerInfo("serverId"));
        BDDAssertions.then(holder.getInfo("serverId")).isNotNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNull();

        holder.setInfo("serverId2", XMLBasedSQSInfoHelper.createServerInfo("serverId2"));
        BDDAssertions.then(holder.getInfo("serverId")).isNotNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNotNull();

        holder.remove("serverId");
        holder.remove("serverId2");
        BDDAssertions.then(holder.getInfo("serverId")).isNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNull();
    }
}