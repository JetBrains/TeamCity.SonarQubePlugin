package jetbrains.buildserver.sonarplugin.manager.projectfeatures;

import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linfar on 03.10.16.
 */
public class SQSInfoImpl extends BaseSQSInfo {
    public SQSInfoImpl(@Nullable final Map<String, String> properties) {
        super(properties);
    }

    public SQSInfoImpl(@NotNull final SProjectFeatureDescriptor fd) {
        super(projectFeatureToMap(fd));
    }

    public SQSInfoImpl(@NotNull String id, @Nullable String name, @Nullable String url, @Nullable String login, @Nullable String password, @Nullable String jdbcUrl, @Nullable String jdbcUsername, @Nullable String jdbcPassword) {
        super(id, name, url, login, password, jdbcUrl, jdbcUsername, jdbcPassword);
    }

    public SQSInfoImpl(@NotNull String id) {
        super(id);
    }

    private static Map<String, String> projectFeatureToMap(@NotNull final SProjectFeatureDescriptor projectFeatureDescriptor) {
        final Map<String, String> res = new HashMap<>(projectFeatureDescriptor.getParameters());
        for (String f : ENCRYPTED_FIELDS) {
            final String val = res.get(f);
            if (!StringUtil.isEmpty(val)) {
                res.put(f, EncryptUtil.unscramble(val));
            }
        }
        return res;
    }
}
