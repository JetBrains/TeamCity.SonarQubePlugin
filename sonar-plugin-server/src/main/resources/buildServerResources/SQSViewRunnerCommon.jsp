<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<div class="parameter">
    Project name: <strong><props:displayValue name="sonarProjectName"/></strong>
</div>

<div class="parameter">
    Project key: <strong><props:displayValue name="sonarProjectKey" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Project version: <strong><props:displayValue name="sonarProjectVersion" emptyValue="not specified"/></strong>
</div>

<div class="parameter">
    Sonar Server ID: <strong><props:displayValue name="sonarServer"/></strong>
</div>

<div class="parameter">
    Project modules: <strong><props:displayValue name="sonarProjectModules" emptyValue="not specified"/></strong>
</div>

<c:if test="${param.includeSourceParameters}">
<div class="parameter">
    Sources location: <strong><props:displayValue name="sonarProjectSources"/></strong>
</div>

<div class="parameter">
    Tests location: <strong><props:displayValue name="sonarProjectTests"/></strong>
</div>

<div class="parameter">
    Binaries location: <strong><props:displayValue name="sonarProjectBinaries"/></strong>
</div>
</c:if>

<div class="parameter">
    Additional parameters: <strong><props:displayValue name="additionalParameters"/></strong>
</div>
