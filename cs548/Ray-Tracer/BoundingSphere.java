
public class BoundingSphere {
	Vector center;
	double radius;
	
	// Bounding sphere for a Bezier surface
	public BoundingSphere(Vector c,double r){
		this.center = c;
		this.radius = r;
	}
	
	public double rayHit(Vector o, Vector d, double tmin, double tmax){
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
				return t;
			t = (front + Math.sqrt(descrim)) / d.dot(d);
			if(t>=tmin && t<=tmax)
				return t;
		}
		return -1.0;
	}
}
