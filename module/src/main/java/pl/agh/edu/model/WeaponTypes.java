package pl.agh.edu.model;

public enum WeaponTypes{
	SNIPER(new Weapon("Sniper Rifle", 150, 20, 25, 50)),
	MACHINE_GUN(new Weapon("Machine gun", 50, 5, 15, 10));
	
	private Weapon weapon;
	
	private WeaponTypes(Weapon weapon){
		this.weapon = weapon;
	}
	
	public Weapon getWeapon(){
		return weapon;
	}
}