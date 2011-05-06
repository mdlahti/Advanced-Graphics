
public class Sphere extends RayObject{
	Vector center;
	double radius;
	MaterialProps mats;
	
	public Sphere(double cx, double cy, double cz, double r, MaterialProps m){
		center = new Vector(cx,cy,cz);
		radius = r;
		mats = m;
	}
	
	/* NOT USED
	public Vector getColor(Vector P){
		double t = ImprovedNoise.turbulence(P,8);
		return mats.Color.mult(t).add(C.mult(1-t));
	}*/
	
	/* return smallest positive t value for nearest hit
	 * otherwise return a negative number for no hit
	 */
	public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax){
		Vector temp = o.sub(center);
		// P(t) = o + t*d
		// descrim = (d*(o-c))^2 - (d*d)((o-c)*(o-c) - r^2)
		double descrim1 = Math.pow(d.dot(temp), 2);
		double descrim2 = (d.dot(d))*(temp.dot(temp) - radius*radius);
		double descrim = descrim1 - descrim2;
		double t = 0;
		if(descrim >= 0){
			//found collision
			double front = -1 * d.dot(temp);
			//find smallest positive t value
			t = (front - Math.sqrt(descrim)) / d.dot(d);
			if(t>=tmin && t<=tmax)
				return (new HitInfo(t,this.normal(o, d, t),mats,mats.tex.getColor(o.add(d.mult(t)))));
			t = (front + Math.sqrt(descrim)) / d.dot(d);
			if(t>=tmin && t<=tmax)
				return (new HitInfo(t,this.normal(o, d, t),mats,mats.tex.getColor(o.add(d.mult(t)))));
		}
		return (new HitInfo(-1.0,null,null,null));
	}
	
	public boolean shadowHit(Vector o, Vector d, double tmin, double tmax){
		if(this.rayHit(o,d,tmin,tmax).t > 0){
			return true;
		}
		return false;
	}
	
	// get the normal vector for this object at the point
	// where o + d*t hits it
	public Vector normal(Vector o, Vector d, double t){
		Vector P = o.add(d.mult(t));
		Vector sphereNorm = (P.sub(center)).div(radius);
		return sphereNorm;
	}
	
	public MaterialProps materials(){
		return mats;
	}
}
