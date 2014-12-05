package pl.agh.edu.model;

public class PlaneType {
	
	private final Weapon weapon;
	
	private final int turnDigreesPerInterval;

	public PlaneType(Weapon weapon, int turnDigreesPerInterval) {
		super();
		this.weapon = weapon;
		this.turnDigreesPerInterval = turnDigreesPerInterval;
	}

}
