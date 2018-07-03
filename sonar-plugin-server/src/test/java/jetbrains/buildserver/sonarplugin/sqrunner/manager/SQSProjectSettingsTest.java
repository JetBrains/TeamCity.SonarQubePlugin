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