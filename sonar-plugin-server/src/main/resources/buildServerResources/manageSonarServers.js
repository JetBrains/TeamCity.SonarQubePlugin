/**
 * Created by linfar on 6/3/14.
 */

SonarPlugin = {
    initPage: function () {
        var $pf = $j(".runnerFormTable input[id='sonar.password_field']");
        $pf.click(function () {
            $pf.val("");
            $pf.attr("data-modified", "modified");
        }).keydown(function () {
            $pf.attr("data-modified", "modified");
        });
        var $pjf = $j(".runnerFormTable input[id='sonar.jdbc.password_field']");
        $pjf.click(function () {
            $pjf.val("");
            $pjf.attr("data-modified", "modified");
        }).keydown(function () {
            $pf.attr("data-modified", "modified");
        });
        $j(".enableDatabaseSettings").click(function () {
            $j(".databaseSettings").show();
            $j(".enableDatabaseSettings").hide();
        });
    },
    encryptPassword: function(pass) {
        return BS.Encrypt.encryptData(pass, $j('#publicKey').val());
    },
    removeServer: function(projectId, serverId) {
        if (!confirm("The profile will be permanently deleted. Proceed?")) {
            return;
        }
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
    editServer: function(data) {
        SonarPlugin.ServerConnectionDialog.showDialog('editSqs', data);
        $j(".runnerFormTable input[id='serverinfo.id']").prop("disabled", true);
    },
    addServer: function(projectId) {
        SonarPlugin.ServerConnectionDialog.showDialog('addSqs', {id: '', name: '', url: '', login: '', password: '', JDBCUsername: '', JDBCPassword: '', projectId: projectId});
        $j(".runnerFormTable input[id='sonar.password_field']").attr("data-modified",  "modified");
        $j(".runnerFormTable input[id='sonar.jdbc.password_field']").attr("data-modified",  "modified");
    },
    ServerConnectionDialog: OO.extend(BS.AbstractWebForm, OO.extend(BS.AbstractModalDialog, {
        getContainer: function () {
            return $('serverInfoDialog');
        },

        formElement: function () {
            return $('serverInfoForm');
        },

        showDialog: function (action, data) {
            $j("input[id='SQSaction']").val(action);
            this.cleanFields(data);
            this.cleanErrors();
            this.showCentered();
        },

        cleanFields: function (data) {
            $j("input[id='serverinfo.id']").val(data.id);
            $j(".runnerFormTable input[id='serverinfo.name']").val(data.name);
            $j(".runnerFormTable input[id='sonar.host.url']").val(data.url);
            $j(".runnerFormTable input[id='sonar.login']").val(data.login);
            $j(".runnerFormTable input[id='sonar.password']").val(data.password).removeAttr("data-modified");
            $j(".runnerFormTable input[id='sonar.password_field']").val(data.password ? "*****" : "").removeAttr("data-modified");
            $j(".runnerFormTable input[id='sonar.jdbc.url']").val(data.JDBCUrl);
            $j(".runnerFormTable input[id='sonar.jdbc.username']").val(data.JDBCUsername);
            $j(".runnerFormTable input[id='sonar.jdbc.password']").val(data.JDBCPassword);
            $j(".runnerFormTable input[id='sonar.jdbc.password_field']").val(data.JDBCPassword ? "*****" : "" );
            if (data.JDBCUrl || data.JDBCUsername || data.JDBCPassword) {
                $j(".runnerFormTable .databaseSettings").show();
                $j(".enableDatabaseSettings").hide();
            } else {
                $j(".runnerFormTable .databaseSettings").hide();
                $j(".enableDatabaseSettings").show();
            }
            $j("#serverInfoForm input[id='projectId']").val(data.projectId);

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

            var parameters = {
                "serverinfo.name": $j(".runnerFormTable input[id='serverinfo.name']").val(),
                "sonar.host.url" : $j(".runnerFormTable input[id='sonar.host.url']").val(),
                "sonar.login": $j(".runnerFormTable input[id='sonar.login']").val(),
                "sonar.jdbc.url": $j(".runnerFormTable input[id='sonar.jdbc.url']").val(),
                "sonar.jdbc.username": $j(".runnerFormTable input[id='sonar.jdbc.username']").val(),
                "projectId": $j("#serverInfoForm #projectId").val(),
                action: $j("#serverInfoForm #SQSaction").val(),
                "serverinfo.id": $j("#serverInfoForm input[id='serverinfo.id']").val(),
                "publicKey" : $j("#serverInfoForm #publicKey").val()
            };

            var $jdbcPasswordField = $j(".runnerFormTable input[id='sonar.jdbc.password_field']");
            if ($jdbcPasswordField.attr("data-modified") == "modified") {
                parameters["sonar.jdbc.password"] = SonarPlugin.encryptPassword($jdbcPasswordField.val());
            } else {
                parameters["sonar.jdbc.password_preserve"] = "true";
            }

            var $passwordField = $j(".runnerFormTable input[id='sonar.password_field']");
            if ($passwordField.attr("data-modified") == "modified") {
                parameters["sonar.password"] = SonarPlugin.encryptPassword($passwordField.val());
            } else {
                parameters["sonar.password_preserve"] = "true";
            }

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
                            if (responseTag.getAttribute("status") == "OK") {
                                shouldClose = true;
                            } else if (responseTag.firstChild == null) {
                                shouldClose = false;
                                alert("Error: empty response");
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
