<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ include file="/include-internal.jsp" %>
<%--@elvariable id="availableServers" type="java.util.List<jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo>"--%>
<%--@elvariable id="projectId" type="java.lang.String"--%>
<div class="manageSQS">
    <c:choose>
        <c:when test="${fn:length(availableServers) > 0}">
            <c:forEach items="${availableServers}" var="server">
                <div class="sqsInfo">
                    <div class="url">URL: <c:out value="${server.url}"/></div>
                    <div class="db">Database: <c:out value="${server.JDBCUrl}"/></div>
                    <c:if test="${not empty server.JDBCUsername}">
                        <div class="dbUser">Database username: <c:out value="${server.JDBCUsername}"/></div>
                    </c:if>
                    <c:if test="${not empty server.JDBCPassword}">
                        <div class="dbPass">Database password: <c:out value="${server.JDBCPassword}"/></div>
                    </c:if>
                    <div class="remove">
                        <forms:addButton id="removeNewServer" onclick="SonarPlugin.removeServer('${projectId}', '${server.id}'); return false">remove</forms:addButton>
                    </div>
                </div>
            </c:forEach>
        </c:when>
        <c:otherwise>
            No SonarQuve Servers registered yet. You can still select a server with additional SonarQube Runner parameters or use local installation.
        </c:otherwise>
    </c:choose>
    <div class="add">
        <div><label for="serverinfo.id">Server id: </label><input type="text" id="serverinfo.id"/></div>
        <div><label for="sonar.host.url">Server url: </label><input type="text" id="sonar.host.url"/></div>
        <div><label for="sonar.jdbc.url">Database url: </label><input type="text" id="sonar.jdbc.url"/></div>
        <div><label for="sonar.jdbc.username">Database username: </label><input type="text" id="sonar.jdbc.username"/></div>
        <div><label for="sonar.jdbc.password">Database password: </label><input type="text" id="sonar.jdbc.password"/></div>
        <forms:addButton id="createNewServer" onclick="SonarPlugin.createServer('${projectId}'); return false">Add new server</forms:addButton>
    </div>
    <script language="javascript">
        SonarPlugin = {
            createServer: function (projectId) {
                var fields = ["serverinfo.id", "sonar.host.url", "sonar.jdbc.url", "sonar.jdbc.username", "sonar.jdbc.password"];

                var params = {
                    action: 'addSqs',
                    projectId: projectId
                };

                for (var i = 0; i < fields.length; ++i) {
                    var val = $j("[id='" + fields[i] + "']").val();
                    if (val != null && !val.empty()) params[fields[i]] = val;
                }

                BS.ajaxRequest('<c:url value="/admin/manageSonarServers.html"/>', {
                    parameters: Object.toQueryString(params)
                });
            },
            removeServer: function(projectId, serverId) {
                BS.ajaxRequest('<c:url value="/admin/manageSonarServers.html"/>', {
                    parameters: Object.toQueryString({
                        action: 'removeSqs',
                        projectId: projectId,
                        'serverinfo.id': serverId
                    })
                });
            }
        };
    </script>
</div>