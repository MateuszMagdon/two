package pl.agh.edu.model;

public class PlaneType {
	private final Weapon weapon;
	
	private final int turnDigreesPerInterval;
	private final int health;
	private final float speed;

	public PlaneType(Weapon weapon, int turnDigreesPerInterval, float speed, int health) {
		this.weapon = weapon;
		this.turnDigreesPerInterval = turnDigreesPerInterval;
		this.speed = speed;
		this.health = health;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}

	public int getTurnDigreesPerInterval() {
		return turnDigreesPerInterval;
	}

	public float getSpeed() {
		return speed;
	}

	public int getHealth() {
		return health;
	}
}
