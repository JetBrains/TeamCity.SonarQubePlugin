/**
 * Created by linfar on 6/3/14.
 */

SonarPlugin = {
    removeServer: function(projectId, serverId) {
        BS.ajaxRequest(window['base_uri'] + '/admin/manageSonarServers.html', {
            parameters: Object.toQueryString({
                action: 'removeSqs',
                projectId: projectId,
                'serverinfo.id': serverId
            }),
            onComplete: function(transport) {
                $("SQservers").refresh();
            }
        });
    },
    editServer: function(id, url, JDBCUrl, JDBCUsername, JDBCPassword) {
        SonarPlugin.ServerConnectionDialog.showDialog('editSQS', id, url, JDBCUrl, JDBCUsername, JDBCPassword);
    },
    addServer: function() {
        SonarPlugin.ServerConnectionDialog.showDialog('addSqs', '', '', '', '', '');
    },
    ServerConnectionDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('serverInfoDialog');
        },

        showDialog: function (action, id, url, JDBCUrl, JDBCUsername, JDBCPassword) {
            $j("input[id='SQSaction']").val(action);
            this.cleanFields(id, url, JDBCUrl, JDBCUsername, JDBCPassword);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (id, url, JDBCUrl, JDBCUsername, JDBCPassword) {
            $j(".runnerFormTable input[id='serverinfo.id']").val(id);
            $j(".runnerFormTable input[id='sonar.host.url']").val(url);
            $j(".runnerFormTable input[id='sonar.jdbc.url']").val(JDBCUrl);
            $j(".runnerFormTable input[id='sonar.jdbc.username']").val(JDBCUsername);
            $j(".runnerFormTable input[id='sonar.jdbc.password']").val(JDBCPassword);
        },

        cleanErrors: function () {
        },

        error: function(message) {
            alert("error: " + message);
        },

        doPost: function() {
            var url = $j('input[id="sonar.host.url"]').val();

            if (url == "") {
                this.error("Please set the server URL");
                return false;
            }

            var parameters = this.serializeParameters();
            var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/manageSonarServers.html', {
                parameters: parameters,
                onComplete: function(transport) {
                    $("SQservers").refresh();
                    dialog.close();
                }
            });

            return false;
        }
    }))
};
