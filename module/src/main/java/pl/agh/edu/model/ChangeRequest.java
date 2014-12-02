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
	 * Whether the shot was fired.
	 */
	private final boolean shotFired;

	public ChangeRequest(int playerId, int directionDelta, boolean shotFired) {
		this.playerId = playerId;
		this.directionDelta = directionDelta;
		this.shotFired = shotFired;
	}

	public int getDirectionDelta() {
		return directionDelta;
	}

	public boolean isShotFired() {
		return shotFired;
	}

	public int getPlayerId() {
		return playerId;
	}
}
