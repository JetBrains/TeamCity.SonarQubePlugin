<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:useBean id="constants" class="jetbrains.buildserver.sonarplugin.sqrunner.tool.SonarQubeScannerConstants"/>

<jsp:include page="../SQSEditRunnerCommon.jsp">
    <jsp:param name="selectToolFragment" value="/tools/editToolUsage.html?toolType=${constants.toolTypeId}&versionParameterName=${constants.sonarQubeScannerVersionParameter}&class=longField"/>
    <jsp:param name="includeSourceParameters" value="${true}"/>
</jsp:include>


<props:javaSettings/>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>
