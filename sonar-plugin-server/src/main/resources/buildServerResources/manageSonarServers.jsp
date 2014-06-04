<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include-internal.jsp" %>
<%--@elvariable id="availableServers" type="java.util.List<jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo>"--%>
<%--@elvariable id="projectId" type="java.lang.String"--%>
<div class="manageSQS">
    <h2 class="noBorder">SonarQube Server profiles</h2>
    <div class="grayNote">Profiles to connect to SonarQube Servers</div>

    <bs:refreshable containerId="SQservers" pageUrl="${pageUrl}">
        <div class="sqsList">
            <c:choose>
                <c:when test="${fn:length(availableServers) > 0}">
                    <table class="sqsTable parametersTable">
                        <tr>
                            <th class="id">Name</th>
                            <th class="host">Server</th>
                            <th class="db">Database</th>
                            <th class="actions" colspan="2">Manage</th>
                        </tr>
                        <c:forEach items="${availableServers}" var="server">
                            <tr class="sqsInfo">
                                <td class="name"><c:out value="${server.id}"/></td>
                                <td class="url"><c:out value="${server.url}"/></td>
                                <td class="db">
                                    <div class="url"><c:out value="${server.JDBCUrl}"/></div>
                                    <c:if test="${not empty server.JDBCUsername}">
                                        <div class="dbUser">Username: <c:out value="${server.JDBCUsername}"/></div>
                                    </c:if>
                                    <c:if test="${not empty server.JDBCPassword}">
                                        <div class="dbPass">Password: <c:out value="${server.JDBCPassword}"/></div>
                                    </c:if>
                                </td>
                                <td class="remove">
                                    <a id="removeNewServer" href="#"
                                       onclick="SonarPlugin.removeServer('${projectId}', '${server.id}'); return false">remove</a>
                                </td>
                                <td class="edit">
                                    <a id="editServer" href="#"
                                       onclick="SonarPlugin.editServer('${server.id}', '${server.url}',
                                               '${server.JDBCUrl}', '${server.JDBCUsername}', '${server.JDBCPassword}'); return false">edit</a>
                                </td>
                            </tr>
                        </c:forEach>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="noSqsFound">
                        No SonarQuve Servers registered yet. You can still select a server with additional SonarQube Runner parameters or use local installation.
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </bs:refreshable>

    <div class="add">
        <forms:addButton id="createNewServer" onclick="SonarPlugin.addServer(); return false">Add new server</forms:addButton>
    </div>

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
                    <th>Name</th>
                    <td>
                        <div><input type="text" id="serverinfo.id" name="serverinfo.id"/></div>
                    </td>
                </tr>
                <tr>
                    <th>URL<l:star/></th>
                    <td>
                        <div><input type="text" id="sonar.host.url" name="sonar.host.url"/></div>
                    </td>
                </tr>
                <tr class="groupingTitle">
                    <td colspan="2">Database settings</td>
                </tr>
                <tr>
                    <th>JDBC URL</th>
                    <td>
                        <div><input type="text" id="sonar.jdbc.url" name="sonar.jdbc.url"/></div>
                    </td>
                </tr>
                <tr>
                    <th>Username</th>
                    <td>
                        <div><input type="text" id="sonar.jdbc.username" name="sonar.jdbc.username"/></div>
                    </td>
                </tr>
                <tr>
                    <th>Password</th>
                    <td>
                        <div><input type="text" id="sonar.jdbc.password" name="sonar.jdbc.password"/></div>
                    </td>
                </tr>
            </table>
            <input type="hidden" name="action" id="SQSaction" value="addSqs"/>
            <input type="hidden" name="projectId" value="${projectId}"/>
            <div class="popupSaveButtonsBlock">
                <forms:submit id="serverInfoDialogSubmit" label="Save"/>
                <forms:cancel onclick="SonarPlugin.ServerConnectionDialog.close()"/>
            </div>
        </forms:multipartForm>
    </bs:dialog>
</div>