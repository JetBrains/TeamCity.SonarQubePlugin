<%@ taglib prefix="util" uri="/WEB-INF/functions/util" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--@elvariable id="sonar_url" type="java.lang.String"--%>
<c:if test="${not empty sonar_url}">
    <tr>
        <td class="st">
            <a href="${util:escapeUrlForQuotes(sonar_url)}">view in sonar</a>
        </td>
    </tr>
</c:if>