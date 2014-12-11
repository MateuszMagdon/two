(function () {
    var app = angular.module('app')
        .controller('chatController', ["$scope", "$rootScope", "vertxEventBusService", function ($scope, $rootScope, vertxEventBusService) {
            var _self = this;
            var user = {name: "czesław", group: "red"};
            var users = [];

            var eb = vertxEventBusService;
            var g_messages = [];
            window.ebus = eb;
            _self.logged = false;

            var createEmptyMessage = function () {
                return {from: "", message: "", class: "normal", date: null};
            };

            function scrollChatWindow() {
                var $messageList = $('#messageList');
                $messageList.scrollTop($messageList.find('ul').height());
            }

            this.messages = g_messages;
            var message = $scope.message = createEmptyMessage();

            $scope.users = users;

            $scope.send = function () {
                var address = 'chat.message';
                message.from = user.name;
                if (message.message.indexOf('/whisper ') == 0) {
                    message.message = message.message.substring(9);
                    var to = message.message.substring(0, message.message.indexOf(' '));
                    message.message = message.message.substring(message.message.indexOf(' '));
                    address = 'chat.message.' + to;
                    message.class = "toWhisper";
                    message.date = new Date();

                    var cloned = {};
                    angular.copy(message, cloned);
                    cloned.from = "Whisper to: " + to;
                    g_messages.push(cloned);
                }

                if (message.message.indexOf('/group') == 0) {
                    message.message = message.message.substring(6);
                    address = 'chat.message.' + user.group;
                }

                eb.publish(address, message);
                $scope.message = message = createEmptyMessage();
                setTimeout(function () { // hack :/
                    scrollChatWindow();
                }, 10);
            };

            $scope.usernameClicked = function (user) {
                message.message = '/whisper ' + user.nickName + ' ';
                $('#msg').focus();
            };

            var handleMsg = function (msg) {
                msg.date = new Date();
                g_messages.push(msg);
                scrollChatWindow();
            };

            $rootScope.$on("logged", function (event, player) {
                _self.logged = true;
                user.name = player.login;
                user.group = player.group;

                eb.send("game.players", {}).then(function (reply) {
                    reply = eval('(' + reply + ')');
                    $scope.users = reply;
                });

                eb.addListener("chat.message", function (msg) {
                    handleMsg(msg);
                });

                eb.addListener("chat.message." + user.name, function (msg) {
                    msg.class = "whisper";
                    handleMsg(msg);
                });

                eb.addListener("chat.message." + user.group, function (msg) {
                    msg.class = "group";
                    handleMsg(msg);
                });

                eb.addListener("client.connected", function (message) {
                    console.log(message);
                    $scope.users.push({nickName: message.login, team: message.group, points: 0});
                });

                eb.addListener("client.disconnected", function (message) {
                    console.log(message);
                    for (var index in $scope.users) {
                        if ($scope.users[index].nickName == message.login) {
                            $scope.users.splice(index, 1);
                            break;
                        }
                    }
                });
            });

            $rootScope.$on("disconnected", function () {
                _self.logged = false;
            });
        }]);


    $(document).ready(function () {
        $(window).keyup(function (event) {
            var msgInput = $('#msg');
            if (event.keyCode == 84) {
                msgInput.focus();
            }

            if (event.keyCode == 27 && msgInput.is(":focus")) {
                msgInput.blur();
            }
        });
    });
})();