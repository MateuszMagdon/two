package pl.agh.edu.model;

/**
 * GameObject representing a plane a player is controlling.
 */
public final class Plane extends GameObject<Plane> {

	/**
	 * Static type of plane info
	 */
	private final PlaneType planeType;

	/**
	 * Health, in range (0, 100).
	 */
	private final int health;

	/**
	 * whether Player holds the fire button down
	 */
	private final boolean firingEnabled;

	/**
	 * last time interval at which the plane shot weapon
	 */
	private final long lastFiredAt;

	private final ChangeRequest.Turn turn;

	public Plane(PlaneType planeType, float x, float y, float direction,
			float speed, Player player, int health, boolean firingEnabled,
			long lastFiredAt, ChangeRequest.Turn turn) {
		super(x, y, direction, speed, player);
		this.health = health;
		this.planeType = planeType;
		this.firingEnabled = firingEnabled;
		this.lastFiredAt = lastFiredAt;
		this.turn = turn;
	}

	@Override
	public Plane moveTo(float x, float y) {
		return new Plane(getPlaneType(), x, y, getDirection(),
				getSpeed(), getPlayer(), getHealth(), getFiringEnabled(),
				getLastFiredAt(), getTurn());
	}

	/**
	 * To calculate degreesToAdd use PlaneType.turnDigreesPerInterval and turn values
	 */
	@Override
	public Plane changeDirection(float degreesToAdd) {
		return new Plane(getPlaneType(), getX(), getY(), getDirection()
				+ degreesToAdd, getSpeed(), getPlayer(), getHealth(),
				getFiringEnabled(), getLastFiredAt(), getTurn());
	}

	/**
	 * at timeInterval bullet was generated
	 * 
	 * @param timeInterval
	 * @return
	 */
	public Plane shotFired(long timeInterval) {
		return new Plane(getPlaneType(), getX(), getY(), getDirection(),
				getSpeed(), getPlayer(), getHealth(), getFiringEnabled(),
				timeInterval, getTurn());
	}

	/**
	 * Returns new plane instance with health subtracted.
	 */
	public Plane subtractHealth(int health) {
		return new Plane(getPlaneType(), getX(), getY(), getDirection(),
				getSpeed(), getPlayer(), getHealth() - health,
				getFiringEnabled(), getLastFiredAt(), getTurn());
	}

	public Plane changeFiringState(boolean fireButtonPressed) {
		return new Plane(getPlaneType(), getX(), getY(), getDirection(),
				getSpeed(), getPlayer(), getHealth(), fireButtonPressed,
				getLastFiredAt(), getTurn());
	}

	public Plane turn(ChangeRequest.Turn turn) {
		return new Plane(getPlaneType(), getX(), getY(), getDirection(),
				getSpeed(), getPlayer(), getHealth(), getFiringEnabled(),
				getLastFiredAt(), turn);
	}

	public Plane handleChangeRequest(ChangeRequest change) {
		return turn(change.getDirectionDelta()).changeFiringState(
				change.isFiringEnabled());
	}

	public int getHealth() {
		return health;
	}

	public boolean getFiringEnabled() {
		return firingEnabled;
	}

	public long getLastFiredAt() {
		return lastFiredAt;
	}

	public PlaneType getPlaneType() {
		return planeType;
	}

	public ChangeRequest.Turn getTurn() {
		return turn;
	}
}
