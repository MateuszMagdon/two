package pl.agh.edu.model;

public enum PlaneTypes {
	
	STANDARD(new PlaneType(WeaponTypes.MACHINE_GUN.getWeapon(), 4)),
	SNIPER(new PlaneType(WeaponTypes.SNIPER.getWeapon(), 2));
	
	private PlaneType planeType;
	
	private PlaneTypes(PlaneType plane){
		this.planeType = plane;
	}
	
	public PlaneType getPlaneType(){
		return planeType;
	}

}
