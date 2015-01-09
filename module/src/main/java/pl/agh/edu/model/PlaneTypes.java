package pl.agh.edu.model;

public enum PlaneTypes {
	
	STANDARD(new PlaneType(WeaponTypes.MACHINE_GUN.getWeapon(), 100, 180.f));
	
	private PlaneType planeType;
	
	private PlaneTypes(PlaneType plane){
		this.planeType = plane;
	}
	
	public PlaneType getPlaneType(){
		return planeType;
	}

}
