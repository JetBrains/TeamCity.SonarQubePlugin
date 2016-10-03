package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildserver.sonarplugin.sqrunner.manager.factories.SQSInfoHolder;
import org.assertj.core.api.BDDAssertions;

/**
 * Created by linfar on 03.10.16.
 */
public class SQSProjectSettingsTest {
    public void test() {
        final SQSInfoHolder holder = new SQSProjectSettings();
        BDDAssertions.then(holder.getInfo("serverId")).isNull();

        holder.setInfo("serverId", SQSInfoFactory.createServerInfo("serverId"));
        BDDAssertions.then(holder.getInfo("serverId")).isNotNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNull();

        holder.setInfo("serverId2", SQSInfoFactory.createServerInfo("serverId2"));
        BDDAssertions.then(holder.getInfo("serverId")).isNotNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNotNull();

        holder.remove("serverId");
        BDDAssertions.then(holder.getInfo("serverId")).isNull();
        BDDAssertions.then(holder.getInfo("serverId2")).isNull();
    }
}