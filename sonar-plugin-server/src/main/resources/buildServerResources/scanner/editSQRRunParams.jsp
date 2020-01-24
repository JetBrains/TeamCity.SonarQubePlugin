<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<jsp:include page="../SQSEditRunnerCommon.jsp">
    <jsp:param name="selectToolFragment" value="/tools/editToolUsage.html?toolType=sonar-qube-scanner&versionParameterName=teamcity.tool.sonarquberunner&class=longField"/>
    <jsp:param name="includeSourceParameters" value="${true}"/>
</jsp:include>

<props:javaSettings/>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>
