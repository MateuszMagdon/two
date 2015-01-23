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

                var $HUD = $('#HUD');
                _self.HUD = $HUD[0].getContext('2d');
                setTimeout(function() {
                    $HUD.attr('width', $HUD.width());
                    $HUD.attr('height', $HUD.height());
                }, 200);

                vertxEventBusService.on("two.clients", function (gameUpdated) {
                    game = gameUpdated;
                    _self.plane = getPlayersPlane(gameUpdated, _self.login);
                    updateCanvas(_self.canvas, _self.scaleData, _self.login, game, board);
                    updateHUD(_self.HUD, _self.plane);
                });
            });

            $rootScope.$on("disconnected", function () {
                _self.logged = false;
            });
        }]);
})();

function updateHUD(hud, plane) {
    var $HUD = $('#HUD');
    hud.clearRect(0,0,$HUD.width(),$HUD.height());

    // ramka
    hud.beginPath();
    var barWidth = $HUD.width() / 2 - 20;
    var barHeight = $HUD.height() - 20;

    var health = 0;
    if(plane != undefined) {
        health = plane.health / plane.planeType.health;
    }

    var hp = barHeight*health;

    hud.clearRect(0,0,$HUD.width(),$HUD.height());

    hud.rect(15, 10, barWidth, barHeight);
    hud.lineWidth = 2;
    hud.strokeStyle = 'black';
    hud.stroke();

    // wypełnienie
    hud.beginPath();
    hud.rect(15, 10+(barHeight-hp), barWidth, hp);
    hud.fillStyle = 'red';
    hud.fill();

    var timeP = 0;
    if(plane != undefined) {
        timeP = (new Date().getTime()-plane.lastFiredAt)/plane.planeType.weapon.minTimeBetweenShots;
        if(timeP>1) {
            timeP = 1;
        }
    }

    var time = barHeight*timeP;

    // ramka
    hud.beginPath();
    hud.rect($HUD.width()/2+5, 10, barWidth, barHeight);
    hud.lineWidth = 2;
    hud.strokeStyle = 'black';
    hud.stroke();

    // wypełnienie
    hud.beginPath();
    hud.rect($HUD.width()/2+5, 10+(barHeight-time), barWidth, time);
    hud.fillStyle = 'yellow';
    hud.fill();
}

function getPlayersPlane(game, login) {
    for(i in game.planes) {
        if(game.planes[i].player.nickName == login) {
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

    window.requestAnimationFrame(function() {
    	animFrame(canvas);
    });
    return canvas
}

var allObjects = [];
var lastUpdate = null;

function animFrame(canvas) {
	var delta = (new Date()).getTime() - lastUpdate;
	lastUpdate = (new Date()).getTime();
	var slice = 500;
	var chunk = delta / slice;
	allObjects.forEach(function(o) {
		var numPixels = o.speed * chunk;
		var angle = (o.angle) * (Math.PI/180)
		var addX = Math.sin(angle) * numPixels;
		var addY = Math.cos(angle) * numPixels;
		o.set({ left: o.left + addX, top: o.top - addY})
	});
	canvas.renderAll();
    window.requestAnimationFrame(function() {
    	animFrame(canvas);
    });
};

function updateCanvas(canvas, scaleData, login, gameObject, board) {
	lastUpdate = (new Date()).getTime();
    function addCanvasObjects(units, canvasObjects, images, scale, isPlanes) {
        var scaleX = getScale(units.horizontal, scale.widthInUnits, scale.width);
        var scaleY = getScale(units.vertical, scale.heightInUnits, scale.height);

        for (var index = 0; index < canvasObjects.length; ++index) {
            var ufo = canvasObjects[index];
            var picture = getPicture(ufo, images, isPlanes);

            var canvasImage = (new fabric.Image(picture, {
                left: ufo.x * units.width_ratio,
                top: ufo.y * units.height_ratio,
                scaleX: scaleX,
                scaleY: scaleY,
                angle: ufo.direction,
                centeredRotation: true
            }));
            canvasImage.speed = ufo.speed;

            canvas.add(canvasImage);
            allObjects.push(canvasImage);
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
        vertical: canvas.getHeight() / scaleData.verticalUnits,
        width_ratio: canvas.getWidth() / 1100,
        height_ratio: canvas.getHeight() / 900
    };

    allObjects = [];
    addCanvasObjects(units, gameObject.planes, images, scaleData.plane, true);
    addCanvasObjects(units, gameObject.bullets, images, scaleData.missile, false);

    countAndSetTeamScores(gameObject);
}

