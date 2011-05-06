
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

public class ImagedSphere extends RayObject{
	Vector center;
	ArrayList<Vector> image;
	double radius;
	MaterialProps mats;
	int W,H;
	
	//XXX not completed or tested
	public ImagedSphere(double cx, double cy, double cz, double r, MaterialProps m, String filename) throws IOException{
		center = new Vector(cx,cy,cz);
		radius = r;
		mats = m;
		String line = null;
		String delims = " ";
		BufferedReader br = new BufferedReader(new FileReader(filename));
    	line = br.readLine();
    	if(line.compareTo("P3") == 0){
    		line = br.readLine();
    		// need to check for comments '#'
    		String[] tok = line.split(delims);
    		W = Integer.parseInt(tok[0]); 
    		H = Integer.parseInt(tok[1]);
    		line = br.readLine();
    		int maxVal = Integer.parseInt(line);
    		image = new ArrayList<Vector>(W*H);
    		while((line = br.readLine()) != null){
    			String[] tokens = line.split(delims);
    			//check for comments '#'
    			//3 values per pixel
    			for(int i = 0; i < tokens.length; i+=3){
    				image.add(new Vector(Integer.parseInt(tokens[i])/255,Integer.parseInt(tokens[i+1])/255,Integer.parseInt(tokens[i+2])/255));
    			}
    		}
    		br.close();
		}	
	}
	
	
	public Vector getColor(Vector P){
		Vector n = (P.sub(center)).div(radius);
		double twopi = 6.28318530718;
		double theta = Math.acos(n.Z);
		double phi = Math.atan2(n.Y, n.X);
		if(phi < 0.0)
			phi += twopi;
		double one_over_twopi = 0.159154943092;
		double one_over_pi = 0.318309886184;
		double U = phi*one_over_twopi;
		double V = (Math.PI - theta) * one_over_pi;
		double u = U - (int)(U);
		double v = V - (int)(V);
		u *= (W-3);
		v *= (H-3);
		int iu = (int)(u);
		int iv = (int)(v);
		double tu = u - iu;
		double tv = v - iv;
		Vector One = (image.get(iv*H + iu)).mult((1-tu)*(1-tv));
		Vector Two = (image.get(iv*H + iu+1)).mult(tu*(1-tv));
		Vector Three = (image.get((iv+1)*H + iu)).mult((1-tu)*tv);
		Vector Four = (image.get((iv+1)*H + iu+1)).mult(tu*tv);
		Vector c = One.add(Two).add(Three).add(Four);
		return c;
	}
	
	/* return smallest positive t value for nearest hit
	 * otherwise return a negative number for no hit
	 */
	public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax){
		Vector temp = o.sub(center);
		// P(t) = o + t*d
		// descrim = (d*(o-c))^2 - (d*d)((o-c)*(o-c) - r^2)
		double descrim1 = Math.pow(d.dot(temp), 2);
		double descrim2 = (d.dot(d))*(temp.dot(temp) - radius*radius);
		//System.out.println(descrim1 + "  " + descrim2);
		double descrim = descrim1 - descrim2;
		double t = 0;
		if(descrim >= 0){
			//found collision
			double front = -1 * d.dot(temp);
			//find smallest positive t value
			t = (front - Math.sqrt(descrim)) / d.dot(d);
			if(t<tmin || t>tmax)
				t = (front + Math.sqrt(descrim)) / d.dot(d);
			if(t>=tmin && t<=tmax){
				Vector P = o.add(d.mult(t));
				Vector n = (P.sub(center)).div(radius);
				double twopi = 6.28318530718;
				double theta = Math.acos(n.Z);
				double phi = Math.atan2(n.Y, n.X);
				if(phi < 0.0)
					phi += twopi;
				double one_over_twopi = 0.159154943092;
				double one_over_pi = 0.318309886184;
				double U = phi*one_over_twopi;
				double V = (Math.PI - theta) * one_over_pi;
				double u = U - (int)(U);
				double v = V - (int)(V);
				u *= (W-3);
				v *= (H-3);
				int iu = (int)(u);
				int iv = (int)(v);
				double tu = u - iu;
				double tv = v - iv;
				Vector One = (image.get(iv*H + iu)).mult((1-tu)*(1-tv));
				Vector Two = (image.get(iv*H + iu+1)).mult(tu*(1-tv));
				Vector Three = (image.get((iv+1)*H + iu)).mult((1-tu)*tv);
				Vector Four = (image.get((iv+1)*H + iu+1)).mult(tu*tv);
				Vector c = One.add(Two).add(Three).add(Four);
				return (new HitInfo(t,n,mats,c));
			}
			/*
			if((front - Math.sqrt(descrim)) > 0){
				t = (front - Math.sqrt(descrim)) / d.dot(d);
				if(t>=tmin && t<=tmax)
					return t;
			}
			t = (front + Math.sqrt(descrim)) / d.dot(d);
			if(t>=tmin && t<=tmax)
				return t;*/
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
		Vector P = o.add(d.mult(t));
		Vector sphereNorm = (P.sub(center)).div(radius);
		return sphereNorm;
	}
	
	public MaterialProps materials(){
		return mats;
	}
}
