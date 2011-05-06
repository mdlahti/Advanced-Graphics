
import java.util.ArrayList;

public class SolidNoise {

	private ArrayList<Vector> grad;
	private int phi[];
	
	public SolidNoise(){
		//rng random
		int i;
		grad = new ArrayList<Vector>(16);
		grad.add(new Vector(1,1,0));
		grad.add(new Vector(-1,1,0));
		grad.add(new Vector(1,-1,0));
		grad.add(new Vector(-1,-1,0));
		
		grad.add(new Vector(1,0,1));
		grad.add(new Vector(-1,0,1));
		grad.add(new Vector(1,0,-1));
		grad.add(new Vector(-1,0,-1));
		
		grad.add(new Vector(0,1,1));
		grad.add(new Vector(0,-1,1));
		grad.add(new Vector(0,1,-1));
		grad.add(new Vector(0,-1,-1));
		
		grad.add(new Vector(1,1,0));
		grad.add(new Vector(-1,1,0));
		grad.add(new Vector(0,-1,1));
		grad.add(new Vector(0,-1,-1));
		
		for(i = 0; i < 16; i++)
			phi[i] = i;
		for(i = 14; i >= 0; i--){
			int target = 1;//(int)(random()*i);
			int temp = phi[i+1];
			phi[i+1] = phi[target];
			phi[target] = temp;
		}
		
	}
	
	public double turbulence(Vector p, int depth){
		double sum = 0.0;
		double weight = 1.0;
		Vector ptemp = p;
		sum = Math.abs(noise(ptemp));
		for(int i = 1; i < depth; i++){
			weight = weight * 2;
			ptemp.setX(p.X * weight);
			ptemp.setY(p.Y * weight);
			ptemp.setZ(p.Z * weight);
			sum += Math.abs(noise(ptemp)) / weight;
		}
		return sum;
	}
	
	public double dturbulence(Vector p, int depth, double d){
		double sum = 0.0;
		double weight = 1.0;
		Vector ptemp = p;
		sum = Math.abs(noise(ptemp)) / d;
		for(int i = 1; i < depth; i++){
			weight = weight * 2;
			ptemp.setX(p.X * weight);
			ptemp.setY(p.Y * weight);
			ptemp.setZ(p.Z * weight);
			sum += Math.abs(noise(ptemp)) / d;
		}
		return sum;
	}
	
	public double noise(Vector p){
		int fi, fj, fk;
		double fu, fv, fw, sum;
		Vector v;
		
		fi = (int)(Math.floor(p.X));
		fj = (int)(Math.floor(p.Y));
		fk = (int)(Math.floor(p.Z));
		fu = p.X - (double)(fi);
		fv = p.Y - (double)(fj);
		fw = p.Z - (double)(fk);
		sum = 0.0;
		
		v = new Vector(fu,fv,fw);
		sum += knot(fi, fj, fk, v);
		
		v = new Vector(fu-1,fv,fw);
		sum += knot(fi+1, fj, fk, v);
		
		v = new Vector(fu, fv-1,fw);
		sum += knot(fi,fj+1,fk,v);
		
		v = new Vector(fu,fv,fw-1);
		sum += knot(fi,fj,fk+1,v);
		
		v = new Vector(fu-1,fv-1,fw);
		sum += knot(fi+1,fj+1,fk,v);
		
		v = new Vector(fu-1,fv,fw-1);
		sum += knot(fi+1,fj,fk+1,v);
		
		v = new Vector(fu,fv-1,fw-1);
		sum += knot(fi,fj+1,fk+1,v);
		
		v = new Vector(fu-1,fv-1,fw-1);
		sum += knot(fi+1,fj+1,fk+1,v);
		
		return sum;
	}
	
	private double omega(double t){
		t = (t > 0.0) ? t : -t;
		return (-6.0*t*t*t*t*t + 15.0*t*t*t*t - 10.0*t*t*t + 1.0);
	}
	
	private Vector gamma(int i, int j, int k){
		int idx;
		idx = phi[Math.abs(k)%16];
		idx = phi[Math.abs(j+idx)%16];
		idx = phi[Math.abs(i+idx)%16];
		return grad.get(idx);
	}
	
	private double knot(int i, int j, int k, Vector v){
		return (omega(v.X)*omega(v.Y)*omega(v.Z)*v.dot(gamma(i,j,k)));
	}
	
	private int intGamma(int i, int j){
		return 0;
	}
}
