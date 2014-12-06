(function() {
    var app = angular.module('app')
        .controller('gameController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService) {
            var _self = this;
            var game = getGame();
            var board = $("#board");

            _self.logged = false;
            _self.canvas = null;
            _self.boardHeight = board.height();
            _self.boardWidth = board.width();
            _self.scaleData = getScaleData();

            $rootScope.$on("logged", function(event, data) {
                _self.logged = true;
                _self.login = data.login;
                _self.group = data.group;
                _self.canvas = setUpBoard(_self.boardHeight, _self.boardWidth);

                updateCanvas(_self.canvas, _self.scaleData, game);
            });

            $rootScope.$on("disconnected", function() {
                _self.logged = false;
            });
        }]);
})();

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

function setUpBoard(height, width) {
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    fabric.Object.prototype.selectable = false;

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(255,255,255)';
    canvas.setHeight(height);
    canvas.setWidth(width);

    //$(document).keydown(function(e) {
    //    var a = plane.getAngle();
    //    switch(e.which) {
    //        case 37:
    //            plane.setAngle(a - 5);
    //            canvas.renderAll(); // if not set will be painted after another action on canvas
    //            break;
    //        case 39:
    //            plane.setAngle(a + 5);
    //            canvas.renderAll();
    //            break;
    //
    //        default: return;
    //    }
    //    e.preventDefault(); // prevent the default action (scroll / move caret)
    //});

    return canvas
}

function updateCanvas(canvas, scaleData, gameObject) {
    function addCanvasObjects(units, gameObjects, pictureName, scale) {
        var scaleX = getScale(units.horizontal, scale.widthInUnits, scale.width);
        var scaleY = getScale(units.vertical, scale.heightInUnits, scale.height);
        var picture = document.getElementById(pictureName);

        for (var index = 0; index < gameObjects.length; ++index) {
            var gameObject = gameObjects[index];

            var image = (new fabric.Image(picture, {
                left: gameObject.x,
                top: gameObject.y,
                scaleX: scaleX,
                scaleY: scaleY,
                angle: gameObject.direction
            }));

            canvas.add(image);
        }
    }

    function getScale(unitInPixels, desiredSizeInUnits, currentSizeInPixels) {
        var desiredSize = unitInPixels * desiredSizeInUnits;
        return desiredSize / currentSizeInPixels;
    }

    canvas.clear();

    var units = {
        horizontal: canvas.getWidth() / scaleData.horizontalUnits,
        vertical: canvas.getHeight() / scaleData.verticalUnits
    };

    addCanvasObjects(units, gameObject.planes, 'plane', scaleData.plane);
    addCanvasObjects(units, gameObject.bullets, 'missile', scaleData.missile);
}

function getGame() {
    return {
        "players" : [{
            "id" : 1,
            "nickName" : "player1",
            "points" : 0,
            "team" : "BLUE"
        }, {
            "id" : 2,
            "nickName" : "player2",
            "points" : 123,
            "team" : "RED"
        }
        ],
        "planes" : [{
            "player" : {
                "id" : 1,
                "nickName" : "player1",
                "points" : 0,
                "team" : "BLUE"
            },
            "planeType" : {
                "weapon" : {
                    "name" : "name",
                    "range" : 10.0,
                    "damage" : 1,
                    "bulletSpeed" : 2.0,
                    "minTimeBetweenShots" : 4
                },
                "turnDigreesPerInterval" : 12
            },
            "health" : 1,
            "firingEnabled" : false,
            "lastFiredAt" : 1,
            "turn" : "LEFT",
            "x" : 100.0,
            "y" : 200.0,
            "direction" : 123,
            "speed" : 1.0
        }, {
            "player" : {
                "id" : 2,
                "nickName" : "player2",
                "points" : 123,
                "team" : "RED"
            },
            "planeType" : {
                "weapon" : {
                    "name" : "name",
                    "range" : 10.0,
                    "damage" : 1,
                    "bulletSpeed" : 2.0,
                    "minTimeBetweenShots" : 4
                },
                "turnDigreesPerInterval" : 12
            },
            "health" : 1,
            "firingEnabled" : true,
            "lastFiredAt" : 1,
            "turn" : "RIGHT",
            "x" : 721.0,
            "y" : 221.0,
            "direction" : 2,
            "speed" : 4.0
        }
        ],
        "bullets" : [{
            "startPositionX" : 1.0,
            "startPositionY" : 2.0,
            "weapon" : {
                "name" : "name",
                "range" : 10.0,
                "damage" : 1,
                "bulletSpeed" : 2.0,
                "minTimeBetweenShots" : 4
            },
            "x" : 108.0,
            "y" : 256.0,
            "direction" : 123,
            "speed" : 2.0
        }, {
            "startPositionX" : 12.0,
            "startPositionY" : 2.0,
            "weapon" : {
                "name" : "name",
                "range" : 10.0,
                "damage" : 1,
                "bulletSpeed" : 2.0,
                "minTimeBetweenShots" : 4
            },
            "x" : 11.0,
            "y" : 212.0,
            "direction" : 1,
            "speed" : 2.0
        }
        ]
    };
}
