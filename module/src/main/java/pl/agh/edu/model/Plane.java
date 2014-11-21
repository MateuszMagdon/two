package pl.agh.edu.model;

/**
 * GameObject representing a plane a player is controlling.
 */
public final class Plane extends GameObject {
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

	/**
	 * Returns new plane instance with health subtracted.
	 */
	public Plane subtractHealth(int health) {
		return new Plane(getX(), getY(), getDirection(), getSpeed(), player,
				this.health - health, weapon);
	}

	/**
	 * Returns new plane instance moved to the given position.
	 */
	public Plane moveTo(float x, float y) {
		return new Plane(x, y, getDirection(), getSpeed(), player, health,
				weapon);
	}

	/**
	 * Returns new plane instance with direction degrees added.
	 */
	public Plane changeDirection(int degreesToAdd) {
		return new Plane(getX(), getY(), getDirection() + degreesToAdd,
				getSpeed(), player, this.health, weapon);
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
