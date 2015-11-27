package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.XmlStorable;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.serverSide.crypt.EncryptUtil;
import jetbrains.buildserver.sonarplugin.Util;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Anndrey Titov on 7/9/14.
 *
 * SQSInfo based on XML definition
 */
class XMLBasedSQSInfo extends BasePropertiesBean implements SQSInfo, XmlStorable {
    public static final String ID = "id";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String JDBC_USERNAME = "jdbcUsername";
    public static final String JDBC_PASSWORD = "jdbcPassword";
    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    private static final String[] OPEN_FIELDS = new String[] {ID, JDBC_URL, JDBC_USERNAME, URL, LOGIN, NAME, DESCRIPTION};
    private static final String[] ENCRYPTED_FIELDS = new String[] {JDBC_PASSWORD, PASSWORD};

    XMLBasedSQSInfo() {
        super(null);
    }

    public XMLBasedSQSInfo(@NotNull final String id,
                           @Nullable final String name,
                           @Nullable final String url,
                           @Nullable final String login,
                           @Nullable final String password,
                           @Nullable final String jdbcUrl,
                           @Nullable final String jdbcUsername,
                           @Nullable final String jdbcPassword) {
        super(null);
        setProperty(ID, id);
        setProperty(NAME, name);
        setProperty(URL, url);
        setProperty(LOGIN, login);
        setProperty(PASSWORD, password);
        setProperty(JDBC_PASSWORD, jdbcPassword);
        setProperty(JDBC_URL, jdbcUrl);
        setProperty(JDBC_USERNAME, jdbcUsername);
    }

    private String get(final String key) {
        return getProperties().get(key);
    }

    @Nullable
    public String getUrl() {
        return get(URL);
    }

    @Nullable
    public String getLogin() {
        return get(LOGIN);
    }

    @Nullable
    public String getPassword() {
        return get(PASSWORD);
    }

    @Nullable
    public String getJDBCUrl() {
        return get(JDBC_URL);
    }

    @Nullable
    public String getJDBCUsername() {
        return get(JDBC_USERNAME);
    }

    @Nullable
    public String getJDBCPassword() {
        return get(JDBC_PASSWORD);
    }

    @NotNull
    public String getId() {
        return get(ID);
    }

    @Nullable
    public String getName() {
        return get(NAME);
    }

    @Nullable
    public String getDescription() {
        return get(DESCRIPTION);
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
