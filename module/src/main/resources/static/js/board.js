(function () {
    var app = angular.module('app')
        .controller('gameController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", "changeRequestService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService, changeRequestService) {
            var _self = this;
            var game;
            var board = $("#board");

            _self.logged = false;
            _self.canvas = null;

            $rootScope.$on("logged", function (event, data) {
                _self.logged = true;
                _self.login = data.login;
                _self.group = data.group;
                _self.scaleData = getScaleData();
                _self.canvas = setUpBoard(changeRequestService);

                vertxEventBusService.on("two.clients", function (gameUpdated) {
                    game = gameUpdated;
                    updateCanvas(_self.canvas, _self.scaleData, _self.login, game, board);
                });
            });

            $rootScope.$on("disconnected", function () {
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

function setUpBoard(changeRequestService) {
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    fabric.Object.prototype.selectable = false;

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(255,255,255)';

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

function updateCanvas(canvas, scaleData, login, gameObject, board) {

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
                angle: ufo.direction
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

    function countAndSetTeamScores(gameObject){
        var redScore = 0,
            blueScore = 0,
            playersCount = gameObject.players.length;

        for (var i = 0; i < playersCount; i++) {
            var player = gameObject.players[i];
            if(player.team == "RED"){
                redScore += player.points;
            } else {
                blueScore += player.points;
            }
        }

        $('#red-team-score').text(redScore);
        $('#blue-team-score').text(blueScore);
    }

    canvas.setHeight( board.height());
    canvas.setWidth( board.width());
    canvas.clear();

    var images = getImages();
    var units = {
        horizontal: canvas.getWidth() / scaleData.horizontalUnits,
        vertical: canvas.getHeight() / scaleData.verticalUnits
    };

    addCanvasObjects(units, gameObject.planes, images, scaleData.plane, true);
    addCanvasObjects(units, gameObject.bullets, images, scaleData.missile, false);

    countAndSetTeamScores(gameObject);
}

