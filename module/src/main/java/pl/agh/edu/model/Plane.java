package pl.agh.edu.model;

/**
 * GameObject representing a plane a player is controlling.
 */
public class Plane extends GameObject {
	/**
	 * The player that controls the plane.
	 */
	private final Player player;

	/**
	 * Health, in range (0, 100).
	 */
	private final int health;

	/**
	 * The weapon this plane has.
	 */
	private final Weapon weapon;

	public Plane(float x, float y, int direction, float speed, Player player,
			int health, Weapon weapon) {
		super(x, y, direction, speed);
		this.player = player;
		this.health = health;
		this.weapon = weapon;
	}

	public Player getPlayer() {
		return player;
	}

	public int getHealth() {
		return health;
	}

	public Weapon getWeapon() {
		return weapon;
	}
}
