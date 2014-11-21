package pl.agh.edu.model;

/**
 * Represents the user playing the game.
 */
public class Player {
	private final String nickName;
	
	private final int points;

	private final Team team;
	
	public Player(String nickName, int points, Team team) {
		this.nickName = nickName;
		this.points = points;
		this.team = team;
	}
	
	public String getNickName() {
		return nickName;
	}

	public int getPoints() {
		return points;
	}

	public Team getTeam() {
		return team;
	}
}
