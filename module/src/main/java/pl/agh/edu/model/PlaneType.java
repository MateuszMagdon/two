package pl.agh.edu.model;

public class PlaneType {
	private final Weapon weapon;
	
	private final int turnDigreesPerInterval;

	public PlaneType(Weapon weapon, int turnDigreesPerInterval) {
		this.weapon = weapon;
		this.turnDigreesPerInterval = turnDigreesPerInterval;
	}
	
	public Weapon getWeapon() {
		return weapon;
	}

	public int getTurnDigreesPerInterval() {
		return turnDigreesPerInterval;
	}
}
