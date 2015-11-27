package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * Class encapsulating several SQSInfos belonging to one SProject
 */
public class SQSProjectSettings implements ProjectSettings {
    public static final String SONARQUBE_SERVER = "sonarqube-server";

    @Nullable
    private Map<String, XMLBasedSQSInfo> mySQSInfos = null;

    @Nullable
    public SQSInfo getInfo(@NotNull final String serverId) {
        return mySQSInfos != null ? mySQSInfos.get(serverId) : null;
    }

    public void setInfo(@NotNull final String serverId, @NotNull final SQSInfo modifiedSerever) {
        if (mySQSInfos == null) {
            mySQSInfos = new HashMap<String, XMLBasedSQSInfo>();
        }
        mySQSInfos.put(serverId, cast(modifiedSerever));
    }

    public boolean remove(String serverId) {
        return mySQSInfos != null && mySQSInfos.remove(serverId) != null;
    }

    public void dispose() {
        // do nothing
    }

    public void readFrom(Element element) {
        final List children = element.getChildren(SONARQUBE_SERVER);
        if (mySQSInfos == null && !children.isEmpty()) {
            mySQSInfos = new HashMap<String, XMLBasedSQSInfo>(children.size());
        }
        for (Object o : children) {
            Element child = (Element)o;
            final XMLBasedSQSInfo info = new XMLBasedSQSInfo();
            info.readFrom(child);
            mySQSInfos.put(info.getId(), info);
        }
    }

    public void writeTo(Element element) {
        if (mySQSInfos != null) {
            for (final XMLBasedSQSInfo info : mySQSInfos.values()) {
                Element serverElement = new Element(SONARQUBE_SERVER);
                info.writeTo(serverElement);
                element.addContent(serverElement);
            }
        }
    }

    @NotNull
    private XMLBasedSQSInfo cast(@NotNull final SQSInfo info) {
        if (info instanceof XMLBasedSQSInfo) {
            return (XMLBasedSQSInfo) info;
        } else {
            return new XMLBasedSQSInfo(info.getId(), info.getName(), info.getUrl(), info.getLogin(), info.getPassword(), info.getJDBCUrl(), info.getJDBCUsername(), info.getJDBCPassword());
        }
    }

    public Collection<? extends SQSInfo> getAll() {
        if (mySQSInfos == null) {
            return Collections.emptyList();
        } else {
            return mySQSInfos.values();
        }
    }
}
