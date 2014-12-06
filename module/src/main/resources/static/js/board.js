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


            $rootScope.$on("logged", function(event, data) {
                _self.logged = true;
                _self.login = data.login;
                _self.group = data.group;
                _self.canvas = setUpBoard(_self.boardHeight, _self.boardWidth);

                updateCanvas(_self.canvas, game);
            });

            $rootScope.$on("disconnected", function() {
                _self.logged = false;
            });
        }]);
})();

function setUpBoard(height, width) {
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    fabric.Object.prototype.selectable = false;

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(255,255,255)';
    canvas.setHeight(height);
    canvas.setWidth(width);

    return canvas
}

function updateCanvas(canvas, gameObject) {

    function prepareGroup(elements, name, scale) {
        for (var index = 0; index < elements.length; ++index) {
            var image = document.getElementById(name),
                item = elements[index],
                result = [];

            var el = (new fabric.Image(image, {
                left: item.x,
                top: item.y,
                scaleX: scale,
                scaleY: scale,
                angle: item.direction
            }));
            canvas.add(el);
        }

        return new fabric.Group(result);
    }

    canvas.clear();

    var planes = prepareGroup(gameObject.planes, 'plane', 0.25);
    var bullets = prepareGroup(gameObject.bullets, 'missile', 0.05);

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
