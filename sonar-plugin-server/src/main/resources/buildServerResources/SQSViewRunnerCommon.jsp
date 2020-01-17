<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props"
%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"
%><%--
  ~ Copyright 2000-2020 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

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
    SonarQube Server ID: <strong><props:displayValue name="sonarServer"/></strong>
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
