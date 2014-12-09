package pl.agh.edu.model;

public enum PlaneTypes {
	
	STANDARD(new PlaneType(WeaponTypes.MACHINE_GUN.getWeapon(), 15)),
	SNIPER(new PlaneType(WeaponTypes.SNIPER.getWeapon(), 10));
	
	private PlaneType planeType;
	
	private PlaneTypes(PlaneType plane){
		this.planeType = plane;
	}
	
	public PlaneType getPlaneType(){
		return planeType;
	}

}
