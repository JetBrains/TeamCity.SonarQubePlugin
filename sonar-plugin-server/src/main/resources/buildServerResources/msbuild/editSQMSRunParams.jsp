<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<jsp:include page="../SQSEditRunnerCommon.tag"/>

<script type="text/javascript">
    $j(function () {
        $j('.sonarServer').click(function() {$j("#error_sonarServer").text("");});
    });
</script>
