package pl.agh.edu.model;

public enum WeaponTypes{
	SNIPER(new Weapon("Sniper Rifle", 800, 20, 150, 6000)),
	MACHINE_GUN(new Weapon("Machine gun", 1000, 5, 200, 3000));
	
	private Weapon weapon;
	
	private WeaponTypes(Weapon weapon){
		this.weapon = weapon;
	}
	
	public Weapon getWeapon(){
		return weapon;
	}
}