package pl.agh.edu.model;

import org.vertx.java.core.json.JsonObject;

import org.vertx.java.core.shareddata.Shareable;
import pl.agh.edu.model.Player;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;

/**
 * Represents full state of the game. It is broadcasted every X milliseconds to
 * every client.
 */
public final class Game implements Shareable {
	private static final Gson GSON = new Gson();

	public static Long GAME_TIME = 60*1000L;
	public static Long BREAK_TIME = 5*1000L;

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

	/**
	 * State of the game
	 */
	private final GameState gameState;

	public Game(ImmutableList<Player> players, ImmutableList<Plane> planes,
			ImmutableList<Bullet> bullets, GameState gameState) {
		this.players = players;
		this.planes = planes;
		this.bullets = bullets;
		this.gameState = gameState;
	}
	
	/**
	 * Returns JSON representation of this object.
	 */
	public JsonObject toJson() {
		return new JsonObject(GSON.toJson(this));
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

	public GameState getGameState() {
		return gameState;
	}
}
