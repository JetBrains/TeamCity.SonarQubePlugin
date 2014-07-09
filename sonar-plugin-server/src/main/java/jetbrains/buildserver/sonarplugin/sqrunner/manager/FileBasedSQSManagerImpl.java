package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildserver.sonarplugin.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrey Titov on 4/4/14.
 *
 * SonarQube Server data manager
 */
public class FileBasedSQSManagerImpl implements SQSManager {
    public static final String PROPERTIES_FILE_EXTENSION = ".properties";

    public synchronized List<SQSInfo> getAvailableServers(final @NotNull ProjectAccessor accessor) {
        SProject currentProject = null;
        final List<SQSInfo> res = new LinkedList<SQSInfo>();
        while((currentProject = accessor.get(currentProject)) != null) {
            processAvailableServers(currentProject, new SQSInfoProcessor() {
                @Override
                public State process(SQSInfo sqsInfo) {
                    res.add(sqsInfo);
                    return State.CONTINUE;
                }
            });
        }
        return res;
    }

    private synchronized void processAvailableServers(final @NotNull SProject project, final @NotNull SQSInfoProcessor processor) {
        final File pluginSettingsDir = getPluginDataDirectory(project);
        if (!pluginSettingsDir.exists()) {
            return;
        }
        final File[] files = pluginSettingsDir.listFiles();
        if (files != null) {
            for (File serverInfo : files) {
                if (!processServerFile(serverInfo, processor)) {
                    break;
                }
            }
        }
    }

    private boolean processServerFile(final @NotNull File serverInfo, final @NotNull SQSInfoProcessor processor) {
        switch (processor.process(serverInfo)) {
            case STOP:
                return false;
            case READ:
                if (!processSQSInfo(serverInfo, processor)) return false;
                break;
            case CONTINUE:
            default:
        }
        return true;
    }

    private boolean processSQSInfo(final @NotNull File serverInfo, final @NotNull SQSInfoProcessor processor) {
        switch (processor.process(readInfoFile(serverInfo))) {
            case STOP:
                return false;
            default:
        }
        return true;
    }

    @Nullable
    public synchronized SQSInfo findServer(final @NotNull ProjectAccessor accessor, final @NotNull String serverId) {
        SProject project = null;
        while ((project = accessor.get(project)) != null) {
            final SQSInfo infoContainer[] = new SQSInfo[] {null};

            processAvailableServers(project, new SQSInfoProcessor() {
                @Override
                public State process(File serverInfo) {
                    final String id = getServerInfoId(serverInfo);
                    if (serverId.equals(id)) {
                        return State.READ;
                    } else {
                        return State.CONTINUE;
                    }
                }

                @Override
                public State process(SQSInfo sqsInfo) {
                    infoContainer[0] = sqsInfo;
                    return State.STOP;
                }
            });

            final SQSInfo info = infoContainer[0];
            if (info != null) {
                return info;
            }
        }
        return null;
    }

    public synchronized void editServer(final @NotNull SProject project,
                                        final @NotNull String serverId,
                                        final @NotNull SQSInfo modifiedSerever) throws IOException {
        removeIfExists(project, serverId);
        addServer(modifiedSerever, project);
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

    public synchronized void addServer(@NotNull final SQSInfo newServer,
                                       @NotNull final SProject toProject) throws IOException {
        final File pluginSettingsDir = getPluginDataDirectory(toProject);
        final String id = newServer.getId();
        if (id == null) {
            throw new ServerIdMissing();
        }
        final File serverInfoFile = new File(pluginSettingsDir, id + PROPERTIES_FILE_EXTENSION);
        if (serverInfoFile.exists()) {
            throw new ServerInfoExists();
        }

        if (Util.assureDirExistence(serverInfoFile.getParentFile())
                || !serverInfoFile.createNewFile()
                || !serverInfoFile.canWrite()) {
            throw new CannotWriteData("Cannot write to directory " + pluginSettingsDir.getAbsolutePath());
        }
        newServer.storeTo(serverInfoFile);
    }

    private static File getPluginDataDirectory(final @NotNull SProject currentProject) {
        return currentProject.getPluginDataDirectory("sonar-qube");
    }

    public synchronized boolean removeIfExists(final @NotNull SProject currentProject,
                                               final @NotNull String id) throws CannotDeleteData {
        final File pluginSettingsDir = getPluginDataDirectory(currentProject);
        if (!pluginSettingsDir.exists()) {
            return false;
        }
        final File[] files = pluginSettingsDir.listFiles(new FilenameFilter() {
            public boolean accept(@NotNull final File dir, @NotNull final String name) {
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

    public static ProjectAccessor recurse(@NotNull final SProject project) {
        return new ProjectAccessor() {
            public SProject get(SProject p) {
                return p == null ? project : p.getParentProject();
            }
        };
    }

    public static ProjectAccessor single(@NotNull final SProject project) {
        return new ProjectAccessor() {
            @Override
            public SProject get(SProject p) {
                return p == null ? project : null;
            }
        };
    }

    public static abstract class SQSInfoProcessor {
        public static enum State {
            CONTINUE,
            STOP,
            READ
        }

        public State process(File serverInfo) {
            return State.READ;
        }

        public State process(SQSInfo sqsInfo) {
            return State.CONTINUE;
        }
    }
}
