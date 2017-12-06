<%@ page import="jetbrains.buildServer.serverSide.crypt.RSACipher" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="afn" uri="/WEB-INF/functions/authz" %>
<%@ include file="/include-internal.jsp" %>
<%--@elvariable id="availableServersMap" type="java.util.Map<jetbrains.buildServer.serverSide.SProject, java.util.List<jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo>"--%>
<%--@elvariable id="projectId" type="java.lang.String"--%>
<%--@elvariable id="userHasPermissionManagement" type="java.lang.Boolean"--%>

<bs:linkScript>
    <%-- Crypto stuff --%>
    /js/crypt/md5.js
    /js/crypt/rsa.js
    /js/crypt/jsbn.js
    /js/crypt/prng4.js
    /js/crypt/rng.js
    /js/bs/encrypt.js
</bs:linkScript>

<div class="manageSQS">
    <h2 class="noBorder">SonarQube Server profiles</h2>
    <div class="grayNote">Profiles to connect to SonarQube Servers</div>

    <c:if test="${userHasPermissionManagement}">
        <div class="add">
            <forms:addButton id="createNewServer" onclick="SonarPlugin.addServer('${projectId}'); return false">Add new server</forms:addButton>
        </div>
    </c:if>

    <bs:refreshable containerId="SQservers" pageUrl="${pageUrl}">
        <div class="sqsList">
            <c:choose>
                <c:when test="${fn:length(availableServersMap) > 0}">
                    <table class="sqsTable parametersTable">
                        <tr>
                            <th class="id">Name</th>
                            <th class="host">SonarQube Server connection</th>
                            <th class="db">Database connection</th>
                            <c:if test="${userHasPermissionManagement}">
                                <th class="actions" colspan="2">Manage</th>
                            </c:if>
                        </tr>
                        <c:forEach items="${availableServersMap}" var="projectServersEntry">
                            <c:forEach items="${projectServersEntry.value}" var="server">
                                <tr class="sqsInfo">
                                    <td class="name"><c:out value="${server.name}"/>
                                        <c:if test="${projectServersEntry.key.externalId != projectId}"> belongs to
                                            <admin:editProjectLink projectId="${projectServersEntry.key.externalId}">
                                                <c:out value="${projectServersEntry.key.name}"/>
                                            </admin:editProjectLink>
                                        </c:if>
                                    </td>
                                    <td class="url">
                                        <div class="url"><c:out value="${server.url}"/></div>
                                        <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                            <c:choose>
                                                <c:when test="${not empty server.login}">
                                                    <div class="login">Username: <c:out value="${server.login}"/></div>
                                                    <div class="password">Password: *****</div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="authentication grayNote">Anonymous</div>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:if>
                                    </td>
                                    <td class="db">
                                        <c:set var="jdbcNotNeeded" value="${empty server.JDBCUrl and empty server.JDBCPassword and empty server.JDBCUsername}"/>
                                        <c:if test="${jdbcNotNeeded}">
                                            <div class="defaultValue grayNote">not needed</div>
                                        </c:if>
                                        <c:if test="${not jdbcNotNeeded}">
                                        <c:choose>
                                            <c:when test="${not empty server.JDBCUrl}"><div class="url"><c:out value="${server.JDBCUrl}"/></div></c:when>
                                            <c:otherwise><div class="defaultValue grayNote">jdbc:h2:tcp://localhost:9092/sonar</div></c:otherwise>
                                        </c:choose>
                                        <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                            <c:choose>
                                                <c:when test="${not empty server.JDBCUsername}"><div class="dbUser">Username: <c:out value="${server.JDBCUsername}"/></div></c:when>
                                                <c:otherwise><div class="defaultValue grayNote">Username: sonar</div></c:otherwise>
                                            </c:choose>
                                            <c:choose>
                                                <c:when test="${not empty server.JDBCPassword}"><div class="dbPass">Password: *****</div></c:when>
                                                <c:otherwise><div class="defaultValue grayNote">Password: sonar</div></c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        </c:if>
                                    </td>
                                    <c:if test="${userHasPermissionManagement && afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                        <td class="remove">
                                            <a id="removeNewServer" href="#"
                                            onclick="SonarPlugin.removeServer('${projectServersEntry.key.externalId}', '${server.id}'); return false">remove...</a>
                                        </td>
                                        <td class="edit">
                                            <a id="editServer" href="#"
                                            onclick="SonarPlugin.editServer({id: '${server.id}', name: '${server.name}', url: '${server.url}', login: '${server.login}',
                                                    password: '${not empty server.login ? "*****" : ""}', JDBCUrl: '${server.JDBCUrl}', JDBCUsername: '${server.JDBCUsername}',
                                                    JDBCPassword: '${not empty server.JDBCUsername ? "*****" : ""}', projectId: '${projectServersEntry.key.externalId}'}); return false">edit</a>
                                        </td>
                                    </c:if>
                                    <c:if test="${userHasPermissionManagement && not afn:permissionGrantedForProject(projectServersEntry.key, 'EDIT_PROJECT')}">
                                        <td colspan="2" class="grayNote"></td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="noSqsFound">
                        No SonarQube Servers registered yet. You can still select a server with additional SonarQube Runner parameters or use local installation.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </bs:refreshable>

    <c:set var="publicKey" value="<%=RSACipher.getHexEncodedPublicKey()%>"/>

    <bs:dialog dialogId="serverInfoDialog"
               dialogClass="serverInfoDialog"
               title="Edit SonarQube Server connection"
               closeCommand="SonarPlugin.ServerConnectionDialog.close()">
        <forms:multipartForm id="serverInfoForm"
                             action="/admin/manageSonarServers.html"
                             targetIframe="hidden-iframe"
                             onsubmit="return SonarPlugin.ServerConnectionDialog.doPost();">

            <table class="runnerFormTable">
                <tr>
                    <th>Name<l:star/></th>
                    <td>
                        <div><input type="text" id="serverinfo.name" name="serverinfo.name"/></div>
                    </td>
                </tr>
                <tr>
                    <th>URL<l:star/></th>
                    <td>
                        <div><input type="text" id="sonar.host.url" name="sonar.host.url"/></div>
                    </td>
                </tr>
                <tr class="groupingTitle">
                    <td colspan="2">Authentication</td>
                </tr>
                <tr>
                    <th>Login</th>
                    <td>
                        <div><input type="text" id="sonar.login" name="sonar.login"/></div>
                    </td>
                </tr>
                <tr>
                    <th>Password</th>
                    <td>
                        <div>
                            <input type="password" id="sonar.password_field" name="sonar.password_field"/>
                            <input type="hidden" id="sonar.password" name="sonar.password"/>
                        </div>
                    </td>
                </tr>
                <tr class="groupingTitle databaseSettings" style="display: none">
                    <td colspan="2">Database settings</td>
                </tr>
                <tr class="databaseSettings" style="display: none">
                    <th>JDBC URL</th>
                    <td>
                        <div><input type="text" id="sonar.jdbc.url" name="sonar.jdbc.url"/></div>
                    </td>
                </tr>
                <tr class="databaseSettings" style="display: none">
                    <th>Username</th>
                    <td>
                        <div><input type="text" id="sonar.jdbc.username" name="sonar.jdbc.username"/></div>
                    </td>
                </tr>
                <tr class="databaseSettings" style="display: none">
                    <th>Password</th>
                    <td>
                        <div>
                            <input type="password" id="sonar.jdbc.password_field" name="sonar.jdbc.password_field"/>
                            <input type="hidden" id="sonar.jdbc.password" name="sonar.jdbc.password"/>
                        </div>
                    </td>
                </tr>
            </table>
            <a href="#" class="enableDatabaseSettings">Obsolete SonarQube Scanner is used which needs Database settings</a>
            <input type="hidden" id="serverinfo.id" name="serverinfo.id"/>
            <input type="hidden" name="action" id="SQSaction" value="addSqs"/>
            <input type="hidden" name="projectId" id="projectId" value="${projectId}"/>
            <input type="hidden" name="publicKey" id="publicKey" value="${publicKey}"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="serverInfoDialogSubmit" label="Save"/>
                <forms:cancel onclick="SonarPlugin.ServerConnectionDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
    <script type="text/javascript">
        $j(function() {
            SonarPlugin.initPage();
        });
    </script>
</div>