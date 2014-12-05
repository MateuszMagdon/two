function setUpBoard() {
    // to make Objects rotate around center - not top-left corner
    fabric.Object.prototype.originX = fabric.Object.prototype.originY = 'center';
    // no object is selectable
    fabric.Object.prototype.selectable = false;
    // custom elementId for all objects
    fabric.Object.prototype.elementId = '';

    var canvas = new fabric.Canvas('gameVisualisation');
    canvas.backgroundColor = 'rgb(190,190,190)';

    var planeElement = document.getElementById('plane');
    var missileElement = document.getElementById('missile');

    var missile = new fabric.Image(missileElement, {
        left: canvas.getWidth() / 2,
        top: canvas.getHeight() / 2 - 100,
        scaleX: 0.05,
        scaleY: 0.05
    });

    var plane = new fabric.Image(planeElement, {
        left: canvas.getWidth() / 2,
        top: canvas.getHeight() / 2 - 100,
        scaleX: 0.25,
        scaleY: 0.25
    });

    canvas.add(plane, missile);

    plane.elementId = '1';
    missile.elementId = '2';

    var elements = [plane, missile];

    var refreshElement = function (element, newVals) {
        element.set({
            top: newVals.y,
            left: newVals.x,
            angle: newVals.direction
        })
    };

    function refresh(newVals) {
        console.log(newVals);
        for (var i = 0 ; i < elements.length ; ++i) {
            var element = elements[i];
            var config = newVals[element.elementId];
            refreshElement(element, config);
        }
        canvas.renderAll();
    }

    function getRandomVal (maxVal) {
        return Math.floor((Math.random() * maxVal) + 1);
    }

    function randomRefresh() {
        refresh({
            '1' : {
                x : getRandomVal(canvas.getWidth()),
                y : getRandomVal(canvas.getHeight()),
                direction: getRandomVal(360)
            },
            '2' : {
                x : getRandomVal(canvas.getWidth()),
                y : getRandomVal(canvas.getHeight()),
                direction: getRandomVal(360)
            }
        });
        setTimeout(randomRefresh, 1000);
    }

    setTimeout(randomRefresh, 1000);

    $(document).keydown(function(e) {
        var a = plane.getAngle();
        switch(e.which) {
            case 37:
                plane.setAngle(a - 5);
                canvas.renderAll(); // if not set will be painted after another action on canvas
                break;
            case 39:
                plane.setAngle(a + 5);
                canvas.renderAll();
                break;

            default: return;
        }
        e.preventDefault(); // prevent the default action (scroll / move caret)
    });

    return canvas
}

(function() {
    var app = angular.module('app')
        .controller('gameController', ["$scope", "$rootScope", "vertxEventBus", "vertxEventBusService", function ($scope, $rootScope, vertxEventBus, vertxEventBusService) {
            var _self = this;
            _self.logged = false;

            _self.canvas = null;

            var height = $("#board").height();
            var width = $("#board").width();

            $rootScope.$on("logged", function() {
                _self.logged = true;

                _self.canvas = setUpBoard();

                _self.canvas.setHeight(height);
                _self.canvas.setWidth(width);

            });

            $rootScope.$on("disconnected", function() {
                _self.logged = false;
            });
        }]);
})();