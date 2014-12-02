package pl.agh.edu.model;

/**
 * Thing that can shot bullets of a type.
 */
public final class Weapon {
	private final String name;

	/**
	 * The range of the weapon in distance units in cartesian metric.
	 */
	private final float range;

	/**
	 * The damage this weapon's bullet deals.
	 */
	private final int damage;

	/**
	 * The number of distance units traveled by the bullet per server time
	 * interval.
	 */
	private final float bulletSpeed;

	public Weapon(String name, float range, int damage, float bulletSpeed) {
		this.name = name;
		this.range = range;
		this.damage = damage;
		this.bulletSpeed = bulletSpeed;
	}

	public String getName() {
		return name;
	}

	public float getRange() {
		return range;
	}

	public int getDamage() {
		return damage;
	}

	public float getBulletSpeed() {
		return bulletSpeed;
	}
}
