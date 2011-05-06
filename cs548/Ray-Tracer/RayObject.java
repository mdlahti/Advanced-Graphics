
public abstract class RayObject {
	
	// abstract class representing ray tracable objects
	public RayObject(){
		
	}
	
	//abstract Vector getColor(Vector P);
	
	abstract HitInfo rayHit(Vector o, Vector d, double tmin, double tmax);
	
	abstract boolean shadowHit(Vector o, Vector d, double tmin, double tmax);
	
	abstract MaterialProps materials();
}
