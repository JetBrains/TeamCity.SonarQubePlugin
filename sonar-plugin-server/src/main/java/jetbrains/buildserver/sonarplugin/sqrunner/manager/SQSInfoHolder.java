package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by linfar on 03.10.16.
 */
public interface SQSInfoHolder {
    @Nullable
    SQSInfo getInfo(@NotNull String serverId);

    void setInfo(@NotNull String serverId, @NotNull SQSInfo modifiedSerever);

    boolean remove(String serverId);
}
