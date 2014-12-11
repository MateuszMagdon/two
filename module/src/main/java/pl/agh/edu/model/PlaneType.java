package pl.agh.edu.model;

public class PlaneType {
	private final Weapon weapon;
	
	private final int turnDigreesPerInterval;

	private final float speed;

	public PlaneType(Weapon weapon, int turnDigreesPerInterval, float speed) {
		this.weapon = weapon;
		this.turnDigreesPerInterval = turnDigreesPerInterval;
		this.speed = speed;
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
}
