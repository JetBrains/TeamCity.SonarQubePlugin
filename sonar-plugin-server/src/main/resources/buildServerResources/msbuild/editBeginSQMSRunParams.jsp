<jsp:useBean id="constants" class="jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants"/>

<jsp:include page="../SQSEditRunnerCommon.jsp">
    <jsp:param name="selectToolFragment" value="/tools/editToolUsage.html?toolType=sonar-scanner-msbuild&versionParameterName=teamcity.tool.sonarqubemsbuild&class=longField"/>
    <jsp:param name="includeSourceParameters" value="${false}"/>
</jsp:include>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>