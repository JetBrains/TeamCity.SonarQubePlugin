<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  ~ Copyright 2000-2021 JetBrains s.r.o.
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

<%--@elvariable id="sonar_url" type="java.lang.String"--%>
<%--@elvariable id="is_sakura_ui" type="boolean"--%>
<c:if test="${not empty sonar_url}">
    <c:choose>
        <c:when test="${is_sakura_ui}">
            <div class="build-summary-link-sonarqube">
                <a href="${util:escapeUrlForQuotes(sonar_url)}" class="ring-link-link" target="_blank">
                    <span class="ring-link-inner">View in SonarQube</span>
                </a>
            </div>
        </c:when>
        <c:otherwise>
            <tr><td></td>
                <td class="st">
                    <a href="${util:escapeUrlForQuotes(sonar_url)}" target="_blank">View in SonarQube</a>
                </td>
            </tr>
        </c:otherwise>
    </c:choose>
</c:if>