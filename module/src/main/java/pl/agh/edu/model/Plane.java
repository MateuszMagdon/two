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
	
	/**
	 * whether Player holds the fire button down
	 */
	private final boolean firingEnabled;
	
	/**
	 * last time interval at which the plane shot weapon
	 */
	private final long lastFiredAt;
	
	public Plane(float x, float y, int direction, float speed, Player player,
			int health, Weapon weapon, boolean firingEnabled, long lastFiredAt) {
		super(x, y, direction, speed);
		this.player = player;
		this.health = health;
		this.weapon = weapon;
		this.firingEnabled = firingEnabled;
		this.lastFiredAt = lastFiredAt;
	}

	/**
	 * Returns new plane instance with health subtracted.
	 */
	public Plane subtractHealth(int health) {
		return new Plane(getX(), getY(), getDirection(), getSpeed(), player,
				this.health - health, weapon, getFiringEnabled(), getLastFiredAt());
	}

	/**
	 * Returns new plane instance moved to the given position.
	 */
	public Plane moveTo(float x, float y) {
		return new Plane(x, y, getDirection(), getSpeed(), player, health,
				weapon, getFiringEnabled(), getLastFiredAt());
	}

	/**
	 * Returns new plane instance with direction degrees added.
	 */
	public Plane changeDirection(int degreesToAdd) {
		return new Plane(getX(), getY(), getDirection() + degreesToAdd,
				getSpeed(), player, this.health, weapon, getFiringEnabled(), getLastFiredAt());
	}
	
	public Plane changeFiringState(boolean fireButtonPressed){
		return new Plane(getX(), getY(), getDirection(),
				getSpeed(), player, this.health, weapon, fireButtonPressed, getLastFiredAt());
	}
	
	public Plane shotFired(long timeInterval){
		return new Plane(getX(), getY(), getDirection(),
				getSpeed(), player, this.health, weapon, getFiringEnabled(), timeInterval);
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
	
	public boolean getFiringEnabled() {
		return firingEnabled;
	}
	
	public long getLastFiredAt() {
		return lastFiredAt;
	}
}
