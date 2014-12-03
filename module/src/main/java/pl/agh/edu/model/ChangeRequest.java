package pl.agh.edu.model;

/**
 * Object representing change player's state change request.
 */
public final class ChangeRequest {
	
	enum Turn{
		LEFT,RIGHT,NONE;
	}
	/**
	 * The id of a player that sends the request.
	 */
	private final int playerId;
	
	/**
	 * Direction change started - turning left, right or not at all.
	 */
	private final Turn directionDelta;
	
	/**
	 * Indicates the change in players button-press
	 * send this change once once per player press of the button and once per release.
	 */
	private final boolean firingEnabled;

	public ChangeRequest(int playerId, Turn directionDelta, boolean shotFired) {
		this.playerId = playerId;
		this.directionDelta = directionDelta;
		this.firingEnabled = shotFired;
	}

	public Turn getDirectionDelta() {
		return directionDelta;
	}

	public boolean isFiringEnabled() {
		return firingEnabled;
	}

	public int getPlayerId() {
		return playerId;
	}
}
