package pl.agh.edu.model;

import pl.agh.edu.model.Player;

import com.google.common.collect.ImmutableList;

/**
 * Represents full state of the game. It is broadcasted every X milliseconds to
 * every client.
 */
public class Game {
	/**
	 * The list of all players.
	 */
	private final ImmutableList<Player> players;

	/**
	 * The list all alive planes.
	 */
	private final ImmutableList<Plane> planes;

	/**
	 * The list of all active bullets.
	 */
	private final ImmutableList<Bullet> bullets;

	public Game(ImmutableList<Player> players, ImmutableList<Plane> planes,
			ImmutableList<Bullet> bullets) {
		this.players = players;
		this.planes = planes;
		this.bullets = bullets;
	}
	
	public ImmutableList<Player> getPlayers() {
		return players;
	}

	public ImmutableList<Plane> getPlanes() {
		return planes;
	}

	public ImmutableList<Bullet> getBullets() {
		return bullets;
	}
}