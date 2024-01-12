<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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