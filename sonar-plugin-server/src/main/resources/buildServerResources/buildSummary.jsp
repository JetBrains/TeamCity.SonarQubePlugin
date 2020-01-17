<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
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

<%--@elvariable id="sonar_url" type="java.lang.String"--%>
<c:if test="${not empty sonar_url}">
    <tr><td></td>
        <td class="st">
            <a href="${util:escapeUrlForQuotes(sonar_url)}">View in SonarQube</a>
        </td>
    </tr>
</c:if>