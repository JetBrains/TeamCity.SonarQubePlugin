<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="util" tagdir="/plugins/sonar-plugin" %>

<util:SQSEditRunnerCommon>
    <jsp:attribute name="toolFragment">
            <jsp:include
                    page="/tools/editToolUsage.html?toolType=${constants.toolTypeId}&versionParameterName=${constants.sonarQubeScannerVersionParameter}&class=longField"/>
    </jsp:attribute>
</util:SQSEditRunnerCommon>


<props:javaSettings/>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>
