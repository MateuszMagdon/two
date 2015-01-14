var gameWrapper = { game: getGame() };
(function () {
    var app = angular.module('app')
        .controller('gameController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", "changeRequestService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService, changeRequestService) {
            var _self = this;
            var board = $("#board");

            _self.logged = false;
            _self.canvas = null;
            _self.HUD = null;
            _self.boardHeight = board.height();
            _self.boardWidth = board.width();
            _self.scaleData = getScaleData();

            $rootScope.$on("logged", function (event, data) {
                _self.logged = true;
                _self.login = data.login;
                _self.group = data.group;
                _self.canvas = setUpBoard(_self.boardHeight, _self.boardWidth, changeRequestService);

                var $HUD = $('#HUD');
                _self.HUD = $HUD[0].getContext('2d');
                setTimeout(function () {
                    $HUD.attr('width', $HUD.width());
                    $HUD.attr('height', $HUD.height());
                }, 200);

                window.requestAnimationFrame(function () {
                    updateCanvas(_self.canvas, _self.scaleData, _self.login, gameWrapper.game)
                });

                vertxEventBusService.on("two.clients", function (gameUpdated) {
                    gameWrapper.game = gameUpdated;
                    _self.plane = getPlayersPlane(gameUpdated, _self.login);
                    //updateCanvas(_self.canvas, _self.scaleData, _self.login, game);
                    updateHUD(_self.HUD, _self.plane);

                    if(gameUpdated.status == "END_GAME") {
                        $('#overlay').show();
                    }
                });

                vertxEventBusService.on("game.over", function () {
                    $('#overlay').show();
                });

                vertxEventBusService.on("game.start", function() {
                    $('#overlay').hide();
                })
            });

            $rootScope.$on("disconnected", function () {
                _self.logged = false;
            });
        }])
        .controller('boardController', function($scope) {
            $scope.game = gameWrapper;
            $scope.filterBlue = { team: "BLUE" };
            $scope.filterRed = { team: "RED" };
        });
})();

function updateHUD(hud, plane) {
    var $HUD = $('#HUD');
    hud.clearRect(0, 0, $HUD.width(), $HUD.height());

    // ramka
    hud.beginPath();
    var barWidth = $HUD.width() / 2 - 20;
    var barHeight = $HUD.height() - 20;

    var health = 0;
    if (plane != undefined) {
        health = plane.health / plane.planeType.health;
    }

    var hp = barHeight * health;

    hud.clearRect(0, 0, $HUD.width(), $HUD.height());

    hud.rect(15, 10, barWidth, barHeight);
    hud.lineWidth = 2;
    hud.strokeStyle = 'black';
    hud.stroke();

    // wypełnienie
    hud.beginPath();
    hud.rect(15, 10 + (barHeight - hp), barWidth, hp);
    hud.fillStyle = 'red';
    hud.fill();

    var timeP = 0;
    if (plane != undefined) {
        timeP = (new Date().getTime() - plane.lastFiredAt) / plane.planeType.weapon.minTimeBetweenShots;
        if (timeP > 1) {
            timeP = 1;
        }
    }

    var time = barHeight * timeP;

    // ramka
    hud.beginPath();
    hud.rect($HUD.width() / 2 + 5, 10, barWidth, barHeight);
    hud.lineWidth = 2;
    hud.strokeStyle = 'black';
    hud.stroke();

    // wypełnienie
    hud.beginPath();
    hud.rect($HUD.width() / 2 + 5, 10 + (barHeight - time), barWidth, time);
    hud.fillStyle = 'yellow';
    hud.fill();
}

function getPlayersPlane(game, login) {
    for (i in game.planes) {
        if (game.planes[i].player.nickName == login) {
            return game.planes[i];
        }
    }
}

function getScaleData() {
    return {
        horizontalUnits: 50,
        verticalUnits: 30,
        plane: {
            widthInUnits: 2,
            heightInUnits: 3,
            width: 213,
            height: 296
        },
        missile: {
            widthInUnits: 1,
            heightInUnits: 1,
            width: 320,
            height: 640
        }
    }
}

function getImages() {
    return {
        plane: {
            red: document.getElementById('planeRed'),
            blue: document.getElementById('planeBlue'),
            player: document.getElementById('planePlayer')
        },
        bullet: document.getElementById('missile')
    }
}

function setUpBoard(height, width, changeRequestService) {
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    fabric.Object.prototype.selectable = false;

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(255,255,255)';
    canvas.setHeight(height);
    canvas.setWidth(width);

    // prevent form flood server by events
    var keyDisAllowed = [];

    $(document).keydown(function (e) {
        if (!$('#msg').is(':focus')) {

            if (keyDisAllowed[e.which] === true) return;
            keyDisAllowed[e.which] = true;

            switch (e.which) {
                case 37: // Left arrow
                    changeRequestService.turnLeft();
                    break;
                case 39: // Right arrow
                    changeRequestService.turnRight();
                    break;
                case 32: // Space
                    changeRequestService.fire();
                    break;
                default:
                    return;
            }
            e.preventDefault(); // prevent the default action (scroll / move caret)
        }
    });

    $(document).keyup(function (e) {
        if (!$('#msg').is(':focus')) {
            keyDisAllowed[e.which] = false;
            switch (e.which) {
                case 37: // Left arrow
                    changeRequestService.endTurn();
                    break;
                case 39: // Right arrow
                    changeRequestService.endTurn();
                    break;
                case 32: // Space
                    changeRequestService.endFire();
                    break;
                default:
                    return;
            }
            e.preventDefault(); // prevent the default action (scroll / move caret)
        }
    });

    return canvas
}

function updateCanvas(canvas, scaleData, login, gameObject) {

    function addCanvasObjects(units, canvasObjects, images, scale, isPlanes) {
        var scaleX = getScale(units.horizontal, scale.widthInUnits, scale.width);
        var scaleY = getScale(units.vertical, scale.heightInUnits, scale.height);

        for (var index = 0; index < canvasObjects.length; ++index) {
            var ufo = canvasObjects[index];
            var picture = getPicture(ufo, images, isPlanes);

            var canvasImage = (new fabric.Image(picture, {
                left: ufo.x,
                top: ufo.y,
                scaleX: scaleX,
                scaleY: scaleY,
                angle: ufo.direction,
                centeredRotation: true
            }));

            canvas.add(canvasImage);
        }
    }

    function getScale(unitInPixels, desiredSizeInUnits, currentSizeInPixels) {
        var desiredSize = unitInPixels * desiredSizeInUnits;
        return desiredSize / currentSizeInPixels;
    }

    function getPicture(gameObject, images, isPlanes) {
        if (!isPlanes) {
            return images.bullet;
        }

        if (gameObject.player.nickName == login) {
            return images.plane.player;
        }

        if (gameObject.player.team == "RED") {
            return images.plane.red;
        }

        return images.plane.blue;
    }

    canvas.clear();

    var images = getImages();
    var units = {
        horizontal: canvas.getWidth() / scaleData.horizontalUnits,
        vertical: canvas.getHeight() / scaleData.verticalUnits
    };

    addCanvasObjects(units, gameObject.planes, images, scaleData.plane, true);
    addCanvasObjects(units, gameObject.bullets, images, scaleData.missile, false);

    window.requestAnimationFrame(function () {
        updateCanvas(canvas, scaleData, login, gameWrapper.game)
    });
}

function getGame() {
    return {
        "players": [{
            "id": 1,
            "nickName": "player1",
            "points": 0,
            "team": "BLUE"
        }, {
            "id": 2,
            "nickName": "player2",
            "points": 123,
            "team": "RED"
        }
        ],
        "planes": [{
            "player": {
                "id": 1,
                "nickName": "player1",
                "points": 0,
                "team": "BLUE"
            },
            "planeType": {
                "weapon": {
                    "name": "name",
                    "range": 10.0,
                    "damage": 1,
                    "bulletSpeed": 2.0,
                    "minTimeBetweenShots": 4
                },
                "turnDigreesPerInterval": 12
            },
            "health": 1,
            "firingEnabled": false,
            "lastFiredAt": 1,
            "turn": "LEFT",
            "x": 100.0,
            "y": 200.0,
            "direction": 123,
            "speed": 1.0
        }, {
            "player": {
                "id": 2,
                "nickName": "player2",
                "points": 123,
                "team": "RED"
            },
            "planeType": {
                "weapon": {
                    "name": "name",
                    "range": 10.0,
                    "damage": 1,
                    "bulletSpeed": 2.0,
                    "minTimeBetweenShots": 4
                },
                "turnDigreesPerInterval": 12
            },
            "health": 1,
            "firingEnabled": true,
            "lastFiredAt": 1,
            "turn": "RIGHT",
            "x": 721.0,
            "y": 221.0,
            "direction": 2,
            "speed": 4.0
        }
        ],
        "bullets": [{
            "startPositionX": 1.0,
            "startPositionY": 2.0,
            "weapon": {
                "name": "name",
                "range": 10.0,
                "damage": 1,
                "bulletSpeed": 2.0,
                "minTimeBetweenShots": 4
            },
            "x": 108.0,
            "y": 256.0,
            "direction": 123,
            "speed": 2.0
        }, {
            "startPositionX": 12.0,
            "startPositionY": 2.0,
            "weapon": {
                "name": "name",
                "range": 10.0,
                "damage": 1,
                "bulletSpeed": 2.0,
                "minTimeBetweenShots": 4
            },
            "x": 11.0,
            "y": 212.0,
            "direction": 1,
            "speed": 2.0
        }
        ]
    };
}

