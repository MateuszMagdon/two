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
		
		/**
		 * Sends left turn change request.
		 * @return {!angular.$q.Promise}
		 */
		this.turnLeft = function() {
			return sendRequest({
				player: loggedUserLogin,
				directionDelta: 'LEFT',
				firingEnabled: false
			});
		};
		
		/**
		 * Sends right turn change request.
		 * @return {!angular.$q.Promise}
		 */
		this.turnRight = function() {
			return sendRequest({
				player: loggedUserLogin,
				directionDelta: 'RIGHT',
				firingEnabled: false
			});
		};
		
		/**
		 * Sends fire change request.
		 * @return {!angular.$q.Promise}
		 */
		this.fire = function() {
			return sendRequest({
				player: loggedUserLogin,
				directionDelta: 'NONE',
				firingEnabled: true
			});
		};
	}
);
