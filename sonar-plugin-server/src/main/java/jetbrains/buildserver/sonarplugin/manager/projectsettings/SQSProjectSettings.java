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

package jetbrains.buildserver.sonarplugin.manager.projectsettings;

import jetbrains.buildServer.serverSide.settings.ProjectSettings;
import jetbrains.buildserver.sonarplugin.manager.SQSInfo;
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

    public void setInfo(@NotNull final String serverId, @NotNull final SQSInfo modifiedServer) {
        if (mySQSInfos == null) {
            mySQSInfos = new HashMap<>();
        }
        mySQSInfos.put(serverId, cast(modifiedServer));
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
            mySQSInfos = new HashMap<>(children.size());
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
