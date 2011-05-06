
public class Plane extends RayObject{

	Vector point;
	Vector norm;
	MaterialProps mats;
	
	public Plane(double px, double py, double pz, double nx, double ny, double nz, MaterialProps m){
		point = new Vector(px,py,pz);
		norm = new Vector(nx,ny,nz);
		mats = m;
	}
	
	/* // NOT USED
	public Vector getColor(Vector P){
		double t = ImprovedNoise.turbulence(P,8);
		return mats.Color.mult(t).add(C.mult(1-t));
	}*/
	
	/* return smallest positive t value for nearest hit
	 * otherwise return a negative number for no hit
	 */
	public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax){
		double t = (point.sub(o)).dot(norm) / d.dot(norm);
		if(t > 0){
			//collision found
			if(t>=tmin && t<=tmax)
				return (new HitInfo(t,norm,mats,mats.tex.getColor(o.add(d.mult(t)))));
		}
		return (new HitInfo(-1.0,null,null,null));
	}
	
	public boolean shadowHit(Vector o, Vector d, double tmin, double tmax){
		if(this.rayHit(o,d,tmin,tmax).t > 0){
			return true;
		}
		return false;
	}
	
	public Vector normal(Vector o, Vector d, double t){
		return norm;
	}
	
	public MaterialProps materials(){
		return mats;
	}
}
