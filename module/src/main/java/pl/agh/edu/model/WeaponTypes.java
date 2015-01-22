package pl.agh.edu.model;

public enum WeaponTypes{
	SNIPER(new Weapon("Sniper Rifle", 600, 40, 350, 6000)),
	MACHINE_GUN(new Weapon("Machine gun", 500, 15, 700, 300));
	
	private Weapon weapon;
	
	private WeaponTypes(Weapon weapon){
		this.weapon = weapon;
	}
	
	public Weapon getWeapon(){
		return weapon;
	}
}