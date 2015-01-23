package pl.agh.edu.model;


/**
 * Represents the user playing the game.
 */
public final class Player {
	private final String nickName;
	
	private final int points;

	private final Team team;
	
	public Player(String nickName, int points, Team team) {
		this.nickName = nickName;
		this.points = points;
		this.team = team;
	}
	
	/**
	 * Returns new Player instance with the number of points added.
	 */
	public Player addPoints(int points) {
		return new Player(nickName, this.points + points, team);
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

	public Player resetPoints() {
		return new Player(nickName, 0, team);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Player)) return false;

		Player player = (Player) o;

		if (!nickName.equals(player.nickName)) return false;
		if (team != player.team) return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = nickName.hashCode();
		result = 31 * result + team.hashCode();
		return result;
	}
}
