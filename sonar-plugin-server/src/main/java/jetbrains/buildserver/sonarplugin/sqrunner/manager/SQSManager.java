package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.Util;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by linfar on 4/4/14.
 *
 * SonarQube Server data manager
 */
public class SQSManager {
    private final @NotNull ProjectManager myProjectManager;

    public SQSManager(@NotNull ProjectManager projectManager) {
        myProjectManager = projectManager;
    }

    public List<SQSInfo> getAvailableServers(SProject currentProject) {
        LinkedList<SQSInfo> res = new LinkedList<SQSInfo>();
        final File pluginSettingsDir = getPluginDataDirectory(currentProject);
        if (!pluginSettingsDir.exists()) {
            pluginSettingsDir.mkdirs();
            return res;
        }
        final File[] files = pluginSettingsDir.listFiles();
        if (files == null) {
            return Collections.emptyList();
        } else {
            for (File serverInfo : files) {
                res.add(readInfoFile(serverInfo));
            }
            return res;
        }
    }

    private SQSInfo readInfoFile(File serverInfo) {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(serverInfo);
            return SQSInfo.from(inStream);
        } catch (Exception ignored) {
            /* ignored */
        } finally {
            Util.close(inStream);
        }
        return null;
    }

    private String getServerInfoId(File serverInfo) {
        final String name = serverInfo.getName();
        final int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            return name.substring(0, idx);
        } else {
            return name;
        }
    }

    public void addServer(@NotNull final SQSInfo newServer, @NotNull final SProject currentProject) throws IOException {
        final File pluginSettingsDir = getPluginDataDirectory(currentProject);
        final String id = newServer.getId();
        if (id == null) {
            throw new ServerIdMissing();
        }
        final File serverInfoFile = new File(pluginSettingsDir, id + ".properties");
        if (serverInfoFile.exists()) {
            throw new ServerInfoExists();
        }

        if (!serverInfoFile.createNewFile() || !serverInfoFile.canWrite()) {
            throw new CannotWriteData("Cannot write to directory " + pluginSettingsDir.getAbsolutePath());
        }
        newServer.storeTo(serverInfoFile);
    }

    private static File getPluginDataDirectory(SProject currentProject) {
        return currentProject.getPluginDataDirectory("sonar-qube");
    }

    public static class CannotWriteData extends IOException {
        public CannotWriteData(String message) {
            super(message);
        }
    }

    public static class ServerInfoExists extends IOException {
    }

    public static class ServerIdMissing extends RuntimeException {
    }
}
