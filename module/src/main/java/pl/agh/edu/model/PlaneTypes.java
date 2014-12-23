package pl.agh.edu.model;

public enum PlaneTypes {
	
	STANDARD(new PlaneType(WeaponTypes.MACHINE_GUN.getWeapon(), 15, 60.f, 100)),
	SNIPER(new PlaneType(WeaponTypes.SNIPER.getWeapon(), 10, 50.f, 75));
	
	private PlaneType planeType;
	
	private PlaneTypes(PlaneType plane){
		this.planeType = plane;
	}
	
	public PlaneType getPlaneType(){
		return planeType;
	}

}
