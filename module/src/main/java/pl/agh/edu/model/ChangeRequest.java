package pl.agh.edu.model;

/**
 * Object representing change player's state change request.
 */
public final class ChangeRequest {
	/**
	 * The id of a player that sends the request.
	 */
	private final int playerId;
	
	/**
	 * The delta in direction degrees.
	 */
	private final int directionDelta;
	
	/**
	 * Indicates the change in players button-press
	 * send this change once once per player press of the button and once per release.
	 */
	private final boolean firingEnabled;

	public ChangeRequest(int playerId, int directionDelta, boolean shotFired) {
		this.playerId = playerId;
		this.directionDelta = directionDelta;
		this.firingEnabled = shotFired;
	}

	public int getDirectionDelta() {
		return directionDelta;
	}

	public boolean isShotFired() {
		return firingEnabled;
	}

	public int getPlayerId() {
		return playerId;
	}
}
