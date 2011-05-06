
import java.util.ArrayList;

public class Bezier extends RayObject{

	MaterialProps mats;
	Bezier left, right;
	BoundingSphere bs;
	boolean leaf = false; //set to true once split small enough
	Triangle T1, T2;
	
	
	public Bezier(BPoints P, MaterialProps m){
		// first get Q, then split Q, use center point from split Q 
		// to figure out the farthest point from center point. Then use 
		// that distance to create bounding sphere, if radius < epsilon then 
		// we have a leaf. If we have a leaf then set up triangles.
		// otherwise subdivide the control points of the bezier into 
		// two halves and create Beziers left and right.
		double minBoundingSphereRadius = 0.04;
		this.mats = m;
		Vec4 Q = eval(0.5, P);
		Vector center = deCastlejau(0.5,Q); // center point of this bezier surface
		// get distance to furthest point
		// furthest point is either P.P0.A, P.P0.D, P.P3.A, or P.P3.D
		double P0A = (P.P0.A.X-center.X)*(P.P0.A.X-center.X)+
				(P.P0.A.Y-center.Y)*(P.P0.A.Y-center.Y)+(P.P0.A.Z-center.Z)*(P.P0.A.Z-center.Z);
		double longest = P0A;
		double P0D = (P.P0.D.X-center.X)*(P.P0.D.X-center.X)+
				(P.P0.D.Y-center.Y)*(P.P0.D.Y-center.Y)+(P.P0.D.Z-center.Z)*(P.P0.D.Z-center.Z);
		if(P0D > longest)
			longest = P0D;
		double P3A = (P.P3.A.X-center.X)*(P.P3.A.X-center.X)+
				(P.P3.A.Y-center.Y)*(P.P3.A.Y-center.Y)+(P.P3.A.Z-center.Z)*(P.P3.A.Z-center.Z);
		if(P3A > longest)
			longest = P3A;
		double P3D = (P.P3.D.X-center.X)*(P.P3.D.X-center.X)+
				(P.P3.D.Y-center.Y)*(P.P3.D.Y-center.Y)+(P.P3.D.Z-center.Z)*(P.P3.D.Z-center.Z);
		if(P3D > longest)
			longest = P3D;
		longest = Math.sqrt(longest);
		//could use Math.sqrt(longest) here instead of all the sqrt's above
		this.bs = new BoundingSphere(center, longest);
		// check if leaf, using hard coded 'epsilon' value
		if(longest < minBoundingSphereRadius)
			this.leaf = true;
		if(leaf){
			// set up leaf triangles and info to be used by rayhit method
			// will be using P00, P03, P30, and P33
			T1 = new Triangle(P.P3.A,P.P3.D,P.P0.A,this.mats);
			T2 = new Triangle(P.P0.D,P.P0.A,P.P3.D,this.mats);
			// P03           P33
			// _______________
			// |            /|
			// |  T2      /  |
			// |        /    |
			// |      /      |
			// |    /        |
			// |  /     T1   |
			// |/____________|
			// P00           P30
		}else{
			// divide bezier surface into halves
			// P0H---P1H------P2H---P3H
			// |       |      |       |
			// |       |      |       |
			// P0G---P1G------P2G---P3G
			// |       |      |       |
			//
			// |       |      |       |
			// P0B---P1B------P2B---P3B
			// |       |      |       |
			// |       |      |       |
			// P0A---P1A------P2A---P3A
			//
			// split entire surface into:
			// _______
			// |  2  |
			// |-----|
			// |  1  |
			// -------
			Vec8 P0sub = subdivideCPS(P.P0);
			Vec8 P1sub = subdivideCPS(P.P1);
			Vec8 P2sub = subdivideCPS(P.P2);
			Vec8 P3sub = subdivideCPS(P.P3);
			
			this.left = new Bezier(new BPoints(new Vec4(P0sub.D,P1sub.D,P2sub.D,P3sub.D),
									new Vec4(P0sub.C,P1sub.C,P2sub.C,P3sub.C),
									new Vec4(P0sub.B,P1sub.B,P2sub.B,P3sub.B),
									new Vec4(P0sub.A,P1sub.A,P2sub.A,P3sub.A)),mats);
			this.right = new Bezier(new BPoints(new Vec4(P0sub.H,P1sub.H,P2sub.H,P3sub.H),
									new Vec4(P0sub.G,P1sub.G,P2sub.G,P3sub.G),
									new Vec4(P0sub.F,P1sub.F,P2sub.F,P3sub.F),
									new Vec4(P0sub.E,P1sub.E,P2sub.E,P3sub.E)),mats);
			
		}
		
	}
	
	//subdivide the control points of a Bezier curve
	//in a different direction than subdivideCPS
	//NOT USED ATM
	/*
	public Vec8 subdivideCPSVectors(Vector P1, Vector P2, Vector P3, Vector P4){
		Vector a,b,c,d,e,f,g;
		a = P1;
		g = P4;
		
		b = (P1.add(P2)).div(2);
		f = (P3.add(P4)).div(2);
		
		Vector temp = (P2.add(P3)).div(2);
		
		c = (b.add(temp)).div(2);
		e = (f.add(temp)).div(2);
		
		d = (c.add(e)).div(2);
		
		return (new Vec8(a,b,c,d,d,e,f,g));
	}*/
	
	//subdivide the control points of a Bezier curve
	public Vec8 subdivideCPS(Vec4 P){
		Vector a,b,c,d,e,f,g;
		a = P.A;
		g = P.D;
		
		b = (P.A.add(P.B)).div(2);
		f = (P.C.add(P.D)).div(2);
		
		Vector temp = (P.B.add(P.C)).div(2);
		
		c = (b.add(temp)).div(2);
		e = (f.add(temp)).div(2);
		
		d = (c.add(e)).div(2);
		
		return (new Vec8(a,b,c,d,d,e,f,g));
	}
	
	//lerp, used to find a point on a curve
	public Vector lerp(double t, Vector A, Vector B){
		return (A.mult(1-t)).add(B.mult(t));
	}
	
	//returns a 3-dimensional point eval'd at t on P
	public Vector deCastlejau(double t, Vec4 P){
		Vector P01 = lerp(t,P.A,P.B);
		Vector P12 = lerp(t,P.B,P.C);
		Vector P23 = lerp(t,P.C,P.D);
		Vector P012 = lerp(t,P01,P12);
		Vector P123 = lerp(t,P12,P23);
		return lerp(t,P012,P123);
	}
	
	//returns a Vec4 eval'd at u for cubic bezier surface
	//computes Q to be "in line" or "parallel" with P0, P1, P2, P3
	// Example: u = .5
	// |      |    |   |      |
	// P10----P11--Q1--P12----P13
	// |      |    |   |      |
	// |      |    |   |      |
	// P00----P01--Q0--P02----P03
	public Vec4 eval(double u, BPoints P){
		Vec4 Q = new Vec4();
		for(int j = 0; j <= 3; j++){
			Q.set(j,deCastlejau(u,P.get(j)));
		}
		return Q;
	}
	
	//split a Vec4 in half and return left and right half in a Vec8
	//Left half = Vec8.A-D  Right half = Vec8.E-H
	public Vec8 split(Vec4 P){
		Vector P01 = lerp(0.5,P.A,P.B);
		Vector P12 = lerp(0.5,P.B,P.C);
		Vector P23 = lerp(0.5,P.C,P.D);
		Vector P012 = lerp(0.5,P01,P12);
		Vector P123 = lerp(0.5,P12,P23);
		Vector Q = lerp(0.5,P012,P123);
		return (new Vec8(P.A,P01,P012,Q,Q,P123,P23,P.D));
	}

	/* NOT USED ATM
	public Vector getColor(Vector P) {
		return mats.Color;
	}*/

	/* Recursive version of rayHit will recursively call rayHit on each 
	 * subdivided portion's bounding spheres of the Bezier. Once a leaf is 
	 * reached the t-value, normal, materials, and color are computed. The 
	 * HitInfo of the leaf with the smallest t value > tmin will be returned, 
	 * otherwise a HitInfo with t-value = -1.0 is returned.
	 */
	public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax) {
		
		HitInfo info = null;
		if(!leaf){
			// not a leaf, check if the ray hits the bounding sphere
			if(bs.rayHit(o, d, tmin, tmax) > 0){
				//bounding sphere is hit by ray, check subdivisions
				HitInfo L,R;
				L = left.rayHit(o, d, tmin, tmax);
				R = right.rayHit(o, d, tmin, tmax);
				if(L.t >= tmin && R.t >= tmin){
					if(L.t < R.t){
						info = L;
					}else{
						info = R;
					}
				}else if(L.t >= tmin){
					info = L;
				}else if(R.t >= tmin){
					info = R;
				}else{
					info = new HitInfo(-1.0,null,null,null);
				}
			}else{
				info = new HitInfo(-1.0,null,null,null);
			}
			//end of if(!leaf)
		}else{
			//leaf bezier, compute normal,color etc.
			// set up info with t, normal, mats, color
			HitInfo temp1 = T1.rayHit(o, d, tmin, tmax);
			HitInfo temp2 = T2.rayHit(o, d, tmin, tmax);
			// check for triangle with nearest intersection
			if(temp1.t >= tmin && temp2.t >= tmin){
				if(temp1.t < temp2.t){
					info = temp1;
				}else{
					info = temp2;
				}
			}else if(temp1.t >= tmin){
				info = temp1;
			}else if(temp2.t >= tmin){
				info = temp2;
			}else{
				//neither triangle actually hit by the ray
				info = new HitInfo(-1.0,null,null,null);
			}
		}
		return info;
	}

	// detect if this object is in shadow
	public boolean shadowHit(Vector o, Vector d, double tmin, double tmax) {
		if(this.rayHit(o,d,tmin,tmax).t > 0){
			return true;
		}
		return false;
	}

	// NOT USED
	public Vector normal(Vector o, Vector d, double t) {
		return null;
	}

	// get this object's material properties
	public MaterialProps materials() {
		return mats;
	}
}
