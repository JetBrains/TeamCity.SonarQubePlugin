<jsp:useBean id="constants" class="jetbrains.buildserver.sonarplugin.msbuild.tool.SQMSConstants"/>

<jsp:include page="../SQSEditRunnerCommon.jsp">
    <jsp:param name="selectToolFragment" value="/tools/editToolUsage.html?toolType=${constants.toolTypeId}&versionParameterName=${constants.sonarQubeScannerVersionParameter}&class=longField"/>
</jsp:include>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>
