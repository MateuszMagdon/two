/**
 * Service used to publish change requests from the game board.
 */
angular.module('app').service(
	'changeRequestService',
	function($rootScope, vertxEventBusService) {
		/**
		 * Keep in sync with Simulator.java
		 * @const {string}
		 */
		var vertxAddress = 'two.server';

		/**
		 * @type {?string}
		 */
		var loggedUserLogin = null;
		
		// Listen to login/logout changes.
		$rootScope.$on('logged', function(event, userInfo) {
			loggedUserLogin = userInfo.login;

			request = {
				player: loggedUserLogin,
				directionDelta: 'NONE',
				firingEnabled: false
			};
		});
	
		$rootScope.$on('disconnected', function() {
			loggedUserLogin = null;
		});
		
		// Helper method to send request to the server.
		var sendRequest = function(object) {
			if (!object.player) {
				throw new Error('Player property in ChangeRequest must not be empty. Please fix.');
			}
			return vertxEventBusService.send(vertxAddress, object);
		};

		var request;
		
		/**
		 * Sends left turn change request.
		 * @return {!angular.$q.Promise}
		 */
		this.turnLeft = function() {
			request.directionDelta = 'LEFT';
			return sendRequest(request);
		};
		
		/**
		 * Sends right turn change request.
		 * @return {!angular.$q.Promise}
		 */
		this.turnRight = function() {
			request.directionDelta = 'RIGHT';
			return sendRequest(request);
		};

		/**
		 * Sends none turn change request.
		 * @return {!angular.$q.Promise}
		 */
		this.endTurn = function() {
			request.directionDelta = 'NONE';
			return sendRequest(request);
		};
		
		/**
		 * Sends fire change request.
		 * @return {!angular.$q.Promise}
		 */
		this.fire = function() {
			request.firingEnabled = true;
			return sendRequest(request);
		};

		/**
		 * Sends end fire change request.
		 * @return {!angular.$q.Promise}
		 */
		this.endFire = function() {
			request.firingEnabled = false;
			return sendRequest(request);
		};
	}
);
