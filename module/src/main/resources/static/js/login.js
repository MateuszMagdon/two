/**
 * Created by kuba cz on 2014-11-26.
 */
(function() {
    var app = angular.module('app')
        .controller('loginController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService) {
            var _self = this;
            _self.available_groups = ["red", "blue"];
            _self.logged = false;
            _self.user = {login: "", group: "red"};

            _self.connect = function (user) {
                vertxEventBusService.send("connect", {'login': user.login, 'group': user.group}).then(function (reply) {
                    if (reply.status === 'ok') {
                        vertxEventBus.EventBus.prototype.sessionID = reply.sessionID;
                        _self.user.login = reply.login;
                        _self.user.group = reply.group;
                        $rootScope.$broadcast('logged', angular.copy(_self.user));
                    } else {
                        console.log("error logging:");
                        console.log(reply)
                    }
                });
            };

            _self.logout = function () {
                vertxEventBusService.send("disconnect", {"sessionID": vertxEventBus.EventBus.prototype.sessionID});
                vertxEventBus.EventBus.prototype.sessionID = null;
                $rootScope.$broadcast('disconnected', angular.copy(_self.user));
                _self.user.login = "";
                _self.user.group = "red";
            };

            $rootScope.$on("logged", function() {
                _self.logged = true;
            });

            $rootScope.$on("disconnected", function() {
                _self.logged = false;
            });
        }]);
})();