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
    editServer: function(id, name, url, JDBCUrl, JDBCUsername, JDBCPassword, projectId) {
        SonarPlugin.ServerConnectionDialog.showDialog('editSqs', id, name, url, JDBCUrl, JDBCUsername, JDBCPassword, projectId);
        $j(".runnerFormTable input[id='serverinfo.id']").prop("disabled", true);
    },
    addServer: function(projectId) {
        SonarPlugin.ServerConnectionDialog.showDialog('addSqs', '', '', '', '', '', '', projectId);
    },
    ServerConnectionDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('serverInfoDialog');
        },

        formElement: function () {
            return $('serverInfoForm');
        },

        showDialog: function (action, id, name, url, JDBCUrl, JDBCUsername, JDBCPassword, projectId) {
            $j("input[id='SQSaction']").val(action);
            this.cleanFields(id, name, url, JDBCUrl, JDBCUsername, JDBCPassword, projectId);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (id, name, url, JDBCUrl, JDBCUsername, JDBCPassword, projectId) {
            $j("input[id='serverinfo.id']").val(id);
            $j(".runnerFormTable input[id='serverinfo.name']").val(name);
            $j(".runnerFormTable input[id='sonar.host.url']").val(url);
            $j(".runnerFormTable input[id='sonar.jdbc.url']").val(JDBCUrl);
            $j(".runnerFormTable input[id='sonar.jdbc.username']").val(JDBCUsername);
            $j(".runnerFormTable input[id='sonar.jdbc.password']").val(JDBCPassword);
            $j("#serverInfoForm input[id='projectId']").val(projectId);

            this.cleanErrors();
        },

        cleanErrors: function () {
            $j("#serverInfoForm .error").remove();
        },

        error: function($element, message) {
            var next = $element.next();
            if (next != null && next.prop("class") != null && next.prop("class").indexOf('error') > 0) {
                next.text(message);
            } else {
                $element.after("<p class='error'>" + message + "</p>");
            }
        },

        doValidate: function() {
            var errorFound = false;

            var url = $j('input[id="sonar.host.url"]');
            if (url.val() == "") {
                this.error(url, "Please set the server URL");
                errorFound = true;
            }

            var name = $j('input[id="serverinfo.name"]');
            if (name.val() == "") {
                this.error(name, "Please set the server name");
                errorFound = true;
            }

            return !errorFound;
        },

        doPost: function() {
            this.cleanErrors();

            if (!this.doValidate()) {
                return false;
            }

            var parameters = this.serializeParameters();
            var dialog = this;

            BS.ajaxRequest(window['base_uri'] + '/admin/manageSonarServers.html', {
                parameters: parameters,
                onComplete: function(transport) {
                    var shouldClose = true;
                    if (transport != null && transport.responseXML != null) {
                        var response = transport.responseXML.getElementsByTagName("response");
                        if (response != null && response.length > 0) {
                            var responseTag = response[0];
                            var error = responseTag.getAttribute("error");
                            if (error != null) {
                                shouldClose = false;
                                alert(error);
                            }
                        }
                    }
                    if (shouldClose) {
                        $("SQservers").refresh();
                        dialog.close();
                    }
                }
            });

            return false;
        }
    }))
};
