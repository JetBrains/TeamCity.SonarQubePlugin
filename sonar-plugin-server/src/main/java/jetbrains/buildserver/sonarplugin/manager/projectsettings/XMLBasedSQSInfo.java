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

import jetbrains.buildServer.XmlStorable;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildserver.sonarplugin.Util;
import jetbrains.buildserver.sonarplugin.manager.BaseSQSInfo;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Andrey Titov on 7/9/14.
 *
 * SQSInfo based on XML definition
 */
public class XMLBasedSQSInfo extends BaseSQSInfo implements XmlStorable {

    XMLBasedSQSInfo() {
        super((Map<String, String>)null);
    }

    public XMLBasedSQSInfo(@NotNull final String id,
                           @Nullable final String name,
                           @Nullable final String url,
                           @Nullable final String login,
                           @Nullable final String password,
                           @Nullable final String jdbcUrl,
                           @Nullable final String jdbcUsername,
                           @Nullable final String jdbcPassword) {
        super(id, name, url, login, password, jdbcUrl, jdbcUsername, jdbcPassword);
    }

    public void readFrom(final Element element) {
        for (final String key : OPEN_FIELDS) {
            setProperty(key, element.getAttributeValue(key));
        }
        for (final String key : ENCRYPTED_FIELDS) {
            final String value = element.getAttributeValue(key);
            if (key != null) {
                String unscrambled;
                try {
                    unscrambled = EncryptUtil.unscramble(value);
                } catch (Exception ignored) {
                    unscrambled = value;
                }
                setProperty(key, unscrambled);
            }
        }
    }

    public void writeTo(final Element serverElement) {
        for (final String key : OPEN_FIELDS) {
            addAttribute(serverElement, key, get(key));
        }
        for (final String key : ENCRYPTED_FIELDS) {
            addAttributeScrambled(serverElement, key, get(key));
        }
    }

    private void addAttribute(Element serverElement, String key, String value) {
        if (!Util.isEmpty(value)) {
            serverElement.setAttribute(key, value);
        }
    }

    private void addAttributeScrambled(Element serverElement, String key, String value) {
        if (!Util.isEmpty(value)) {
            serverElement.setAttribute(key, EncryptUtil.scramble(value));
        }
    }
}
