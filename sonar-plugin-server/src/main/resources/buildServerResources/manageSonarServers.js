/**
 * Created by linfar on 6/3/14.
 */

SonarPlugin = {
    createServer: function (projectId) {
        var fields = ["serverinfo.id", "sonar.host.url", "sonar.jdbc.url", "sonar.jdbc.username", "sonar.jdbc.password"];

        var params = {
            action: 'addSqs',
            projectId: projectId
        };

        for (var i = 0; i < fields.length; ++i) {
            var val = $j("[id='" + fields[i] + "']").val();
            if (val != null && !val.empty()) params[fields[i]] = val;
        }

        BS.ajaxRequest(window['base_uri'] + '/admin/manageSonarServers.html', {
            parameters: Object.toQueryString(params)
        });
    },
    removeServer: function(projectId, serverId) {
        BS.ajaxRequest(window['base_uri'] + '/admin/manageSonarServers.html', {
            parameters: Object.toQueryString({
                action: 'removeSqs',
                projectId: projectId,
                'serverinfo.id': serverId
            })
        });
    }
};
