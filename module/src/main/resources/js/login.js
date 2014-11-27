/**
 * Created by kuba cz on 2014-11-26.
 */
login = angular.module('login', ['knalli.angular-vertxbus'])
    .controller('loginController', function ($scope, vertxEventBus, vertxEventBusService) {
        var _self = this;
        _self.logged = false;
        _self.user = {login: "", group: "red"};

        _self.connect = function (user) {
            //console.log("connect");
            vertxEventBusService.send("connect", {'login': user.login, 'group': user.group}).then(function (reply) {
                if (reply.status === 'ok') {
                    vertxEventBus.sessionID = reply.sessionID;
                    _self.user.login = reply.login;
                    _self.user.group = reply.group;
                    _self.logged = true;
                    console.log("logged:");
                } else {
                    console.log("error logging:")
                }

                console.log(reply)
            });
        };

        _self.logout = function () {
            vertxEventBusService.send("disconnect", {"sessionID": vertxEventBus.sessionID});
            vertxEventBus.sessionID = null;
            _self.user.login = "";
            _self.user.group = "red";
            _self.logged = false;
        }
    });