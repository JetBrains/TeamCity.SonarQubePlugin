<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="sonarProjectModules">Modules: </label></th>
    <td><props:textProperty name="sonarProjectModules" className="longField"/>
    </td>
</tr>
<tr>
    <th class="noBorder"><label for="additionalParameters">Additional parameters: </label></th>
    <td><props:textProperty name="additionalParameters" className="multilineProperty longField"/>
    </td>
</tr>
