<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<tr>
    <th class="noBorder"><label for="sonarProjectName">Project name: </label></th>
    <td><props:textProperty name="sonarProjectName" className="longField"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarProjectKey">Project key: </label></th>
    <td><props:textProperty name="sonarProjectKey" className="longField"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarProjectVersion">Project version: </label></th>
    <td><props:textProperty name="sonarProjectVersion" className="longField"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarProjectSources">Sources location: </label></th>
    <td><props:textProperty name="sonarProjectSources" className="longField"/>
    <bs:vcsTree fieldId="sonarProjectSources"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarServerId">SonarQube Server: </label></th>
    <td>
        <c:choose>
            <c:when test="${not empty bean.sonarServers}">
                <props:selectProperty name="sonarServerId" className="longField">
                    <c:forEach items="${bean.sonarServers}" var="sonarServer">
                        <props:option value="${sonarServer.id}"><c:out value="${sonarServer.description}"/></props:option>
                    </c:forEach>
                </props:selectProperty>
            </c:when>
            <c:otherwise>
                No SonarQube Servers registered.
            </c:otherwise>
        </c:choose></td>
</tr>
<props:javaSettings/>
<tr>
    <th class="noBorder"><label for="sonarProjectTests">Tests location: </label></th>
    <td><props:textProperty name="sonarProjectTests" className="longField"/>
    <bs:vcsTree fieldId="sonarProjectTests"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarProjectModules">Modules: </label></th>
    <td><props:textProperty name="sonarProjectModules" className="longField"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="additionalParameters">Additional parameters: </label></th>
    <td><props:multilineProperty name="additionalParameters" className="longField" linkTitle="any additional parameters to be passed to SonarQube Runner as is" cols="40" rows="3" expanded="true"/>
    </td>
</tr>
