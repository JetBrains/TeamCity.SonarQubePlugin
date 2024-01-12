<%@ include file="/include-internal.jsp"%>


<table class="runnerFormTable">
    <tr>
        <td colspan="2"><em>Support SonarQube <a href="https://docs.sonarqube.org/latest/branches/overview/" target="_blank"
                rel="noopener noreferrer">Branches</a> and <a href="https://docs.sonarqube.org/latest/analysis/pull-request/" target="_blank"
                rel="noopener noreferrer">Pull-Requests</a> analysis by providing <code>SONARQUBE_SCANNER_PARAMS</code> environment variable (requires
                TeamCity v2019.2 and SonarQube <a href="https://www.sonarsource.com/plans-and-pricing/" target="_blank" rel="noopener noreferrer">Developer
                    Edition or above</a>).
        </em></td>
    </tr>
    <tr>
        <th><label>Provider:<l:star /></label></th>
        <td><props:selectProperty name="provider">
                <props:option value="GitHub">GitHub</props:option>
            </props:selectProperty></td>
    </tr>
</table>