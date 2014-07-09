package jetbrains.buildserver.sonarplugin.sqrunner.manager;

import jetbrains.buildServer.XmlStorable;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Anndrey Titov on 7/9/14.
 *
 * SQSInfo based on XML definition
 */
class XMLBasedSQSInfo implements SQSInfo, XmlStorable {
    public static final String ID = "id";
    public static final String JDBC_URL = "jdbcUrl";
    public static final String JDBC_USERNAME = "jdbcUsername";
    public static final String JDBC_PASSWORD = "jdbcPassword";
    public static final String URL = "url";

    private String myId;
    private String myUrl;
    private String myJdbcUrl;
    private String myJdbcUsername;
    private String myJdbcPassword;

    public XMLBasedSQSInfo() {
    }

    public XMLBasedSQSInfo(String myId, String myUrl, String myJdbcUrl, String myJdbcUsername, String myJdbcPassword) {
        this.myId = myId;
        this.myUrl = myUrl;
        this.myJdbcUrl = myJdbcUrl;
        this.myJdbcUsername = myJdbcUsername;
        this.myJdbcPassword = myJdbcPassword;
    }

    @Nullable
    public String getUrl() {
        return myUrl;
    }

    @Nullable
    public String getJDBCUrl() {
        return myJdbcUrl;
    }

    @Nullable
    public String getJDBCUsername() {
        return myJdbcUsername;
    }

    @Nullable
    public String getJDBCPassword() {
        return myJdbcPassword;
    }

    @Nullable
    public String getId() {
        return myId;
    }

    public void readFrom(Element element) {
        myId = element.getAttributeValue(ID);
        myUrl = element.getAttributeValue(URL);
        myJdbcUrl = element.getAttributeValue(JDBC_URL);
        myJdbcUsername = element.getAttributeValue(JDBC_USERNAME);
        myJdbcPassword = element.getAttributeValue(JDBC_PASSWORD);
    }

    public void writeTo(Element serverElement) {
        addAttribute(serverElement, ID, myId);
        addAttribute(serverElement, URL, myUrl);
        addAttribute(serverElement, JDBC_URL, myJdbcUrl);
        addAttribute(serverElement, JDBC_USERNAME, myJdbcUsername);
        addAttribute(serverElement, JDBC_PASSWORD, myJdbcPassword);
    }

    private void addAttribute(Element serverElement, String key, String value) {
        if (value != null) {
            serverElement.setAttribute(key, value);
        }
    }
}
