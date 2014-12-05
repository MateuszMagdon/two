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
            }))
            canvas.add(el);
        }

        return new fabric.Group(result);
    }

    canvas.clear();

    var planes = prepareGroup(gameObject.planes, 'plane', 0.25);
    var bullets = prepareGroup(gameObject.bullets, 'missile', 0.05);

}

function setUpBoard() {
    // to make Objects rotate around center - not top-left corner
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    // no object is selectable
    fabric.Object.prototype.selectable = false;
    // custom elementId for all objects
    fabric.Object.prototype.elementId = '';

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(190,190,190)';

//    $(document).keydown(function(e) {
//        var a = plane.getAngle();
//        switch(e.which) {
//            case 37:
//                plane.setAngle(a - 5);
//                canvas.renderAll(); // if not set will be painted after another action on canvas
//                break;
//            case 39:
//                plane.setAngle(a + 5);
//                canvas.renderAll();
//                break;
//
//            default: return;
//        }
//        e.preventDefault(); // prevent the default action (scroll / move caret)
//    });

    return canvas
}

(function() {
    var app = angular.module('app')
        .controller('gameController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService) {
            var _self = this,
                game = {
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
                        "y" : 2.0,
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
                        "x" : 221.0,
                        "y" : 121.0,
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
                        "x" : 1.0,
                        "y" : 2.0,
                        "direction" : 1,
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
            _self.logged = false;

            _self.canvas = null;

            var height = $("#board").height();
            var width = $("#board").width();

            $rootScope.$on("logged", function() {
                _self.logged = true;

                _self.canvas = setUpBoard();

                _self.canvas.setHeight(height);
                _self.canvas.setWidth(width);

                updateCanvas(_self.canvas, game);
            });

            $rootScope.$on("disconnected", function() {
                _self.logged = false;
            });
        }]);
})();