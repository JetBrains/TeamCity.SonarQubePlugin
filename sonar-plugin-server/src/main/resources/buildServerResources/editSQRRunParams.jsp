<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="bs" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="SonarQube Runner Parameters" className="advancedSetting">
    <tr><th class="noBorder"><label for="sqsChooser">SonarQube Server: </label></th>
        <td>
            <c:choose>
                <%--@elvariable id="servers" type="java.util.List<jetbrains.buildserver.sonarplugin.sqrunner.manager.SQSInfo>"--%>
                <c:when test="${not empty servers}">
                    <forms:select name="sqsChooser" enableFilter="true" className="mediumField">
                        <c:forEach items="${servers}" var="server">
                            <forms:option value="${server.id}"><c:out value="${server.name}"/>: <c:out value="${server.url}"/></forms:option>
                        </c:forEach>
                    </forms:select>
                </c:when>
                <c:otherwise>
                    <span class="smallNote">No SonarQube Server registered yet for this project</span>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
    <tr style="display: none;"><th></th><td><props:textProperty name="sonarServer"/></td></tr>
    <script type="application/javascript">
        (function () {
            var sonarServer = $j('#sonarServer');
            var chooser = $j('#sqsChooser');

            var onSqsChooserChange = function() {
                sonarServer.val($j('#sqsChooser option:selected').val());
            };

            chooser.change(onSqsChooserChange);
            if (sonarServer.val()) {
                chooser.val(sonarServer.val());
            } else {
                sonarServer.val(chooser.val());
            }
        })();
    </script>

    <tr>
        <th class="noBorder"><label for="sonarProjectName">Project name: </label></th>
        <td>
            <props:textProperty name="sonarProjectName" className="longField"/>
            <span class="smallNote">Project name to show at the SonarQube Server.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label for="sonarProjectKey">Project key: </label></th>
        <td>
            <props:textProperty name="sonarProjectKey" className="longField"/>
            <span class="smallNote">Project key used at SonarQube Server to store data.</span>
        </td>
    </tr>
    <tr>
        <th class="noBorder"><label for="sonarProjectVersion">Project version: </label></th>
        <td>
            <props:textProperty name="sonarProjectVersion" className="longField"/>
            <span class="smallNote">Project version.</span>
        </td>
    </tr>

    <tr>
        <th class="noBorder"><label for="sonarProjectSources">Sources location: </label></th>
        <td>
            <props:textProperty name="sonarProjectSources" className="longField"/>
            <bs:vcsTree fieldId="sonarProjectSources"/>
            <span class="smallNote">Path to directory containing sources root.</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th class="noBorder"><label for="sonarProjectTests">Tests location: </label></th>
        <td>
            <props:textProperty name="sonarProjectTests" className="longField"/>
            <bs:vcsTree fieldId="sonarProjectTests"/>
            <span class="smallNote">Path to directory containing tests root.</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th class="noBorder"><label for="sonarProjectBinaries">Binaries location: </label></th>
        <td>
            <props:textProperty name="sonarProjectBinaries" className="longField"/>
            <bs:vcsTree fieldId="sonarProjectBinaries"/>
            <span class="smallNote">Path to directory containing binaries.</span>
        </td>
    </tr>
    <tr class="advancedSetting">
        <th class="noBorder"><label for="sonarProjectModules">Modules: </label></th>
        <td>
            <props:textProperty name="sonarProjectModules" className="longField"/>
            <bs:vcsTree fieldId="sonarProjectModules"/>
            <span class="smallNote">Comma-separated list of modules in the project. Leave blank if project doesn't have module structure. </span>
        </td>
    </tr>
</l:settingsGroup>
<tr class="advancedSetting">
    <th class="noBorder"><label for="additionalParameters">Additional parameters: </label></th>
    <td><props:multilineProperty name="additionalParameters" className="longField" linkTitle="any additional parameters separated with newline to be passed to SonarQube Runner as is" cols="40" rows="3" expanded="true"/>
    </td>
</tr>

<props:javaSettings/>
