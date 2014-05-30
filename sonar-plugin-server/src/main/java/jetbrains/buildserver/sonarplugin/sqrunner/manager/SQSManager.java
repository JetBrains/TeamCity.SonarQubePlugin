package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.Util;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
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
    public static final String PROPERTIES_FILE_EXTENSION = ".properties";

    public synchronized List<SQSInfo> getAvailableServers(final @NotNull SProject currentProject) {
        LinkedList<SQSInfo> res = new LinkedList<SQSInfo>();
        if (currentProject.getParentProject() != null) {
            res.addAll(getAvailableServers(currentProject.getParentProject()));
        }
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

    private SQSInfo readInfoFile(final @NotNull File serverInfo) {
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

    private String getServerInfoId(final @NotNull File serverInfo) {
        final String name = serverInfo.getName();
        final int idx = name.lastIndexOf('.');
        if (idx >= 0) {
            return name.substring(0, idx);
        } else {
            return name;
        }
    }

    public synchronized void addServer(@NotNull final SQSInfo newServer, @NotNull final SProject currentProject) throws IOException {
        final File pluginSettingsDir = getPluginDataDirectory(currentProject);
        final String id = newServer.getId();
        if (id == null) {
            throw new ServerIdMissing();
        }
        final File serverInfoFile = new File(pluginSettingsDir, id + PROPERTIES_FILE_EXTENSION);
        if (serverInfoFile.exists()) {
            throw new ServerInfoExists();
        }

        if (!serverInfoFile.createNewFile() || !serverInfoFile.canWrite()) {
            throw new CannotWriteData("Cannot write to directory " + pluginSettingsDir.getAbsolutePath());
        }
        newServer.storeTo(serverInfoFile);
    }

    private static File getPluginDataDirectory(final @NotNull SProject currentProject) {
        return currentProject.getPluginDataDirectory("sonar-qube");
    }

    public synchronized boolean removeIfExists(final @NotNull SProject currentProject, final @NotNull String id) throws CannotDeleteData {
        final File pluginSettingsDir = getPluginDataDirectory(currentProject);
        if (!pluginSettingsDir.exists()) {
            return false;
        }
        final File[] files = pluginSettingsDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equals(id + PROPERTIES_FILE_EXTENSION);
            }
        });
        if (files == null || files.length == 0) {
            return false;
        } else {
            if (files[0].delete()) {
                return true;
            } else {
                throw new CannotDeleteData("Cannot delete file " + files[0].getAbsolutePath());
            }
        }
    }

    public static class CannotWriteData extends IOException {
        public CannotWriteData(final @NotNull String message) {
            super(message);
        }
    }

    public static class ServerInfoExists extends IOException {
    }

    public static class ServerIdMissing extends RuntimeException {
    }

    public static class CannotDeleteData extends IOException {
        public CannotDeleteData(final @NotNull String message) {
            super(message);
        }
    }
}
