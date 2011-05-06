
public class MarbleTexture extends Texture{

	private double scale = 5.0;
	private int octaves = 8;
	double freq;
	Vector C1, C2, C3;
	
	public MarbleTexture(Vector c1, Vector c2, Vector c3, double stripes_per_unit){
		freq = Math.PI * 10;//stripes_per_unit;
		C1 = c1;
		C2 = c2;
		C3 = c3;
	}
	
	public Vector getColor(Vector p){
		//double temp = scale*ImprovedNoise.noise(p.X,p.Y,p.Z);
		//double temp = scale*ImprovedNoise.turbulence(p.mult(freq),octaves);
		//double t = 2.0*Math.abs(Math.sin((freq*p.X + temp)*Math.PI/180));
		double t = 2.0*ImprovedNoise.turbulence(p, 4);
		//double t = 2.0*Math.abs(Math.cos((Math.PI/180)*(p.X + Math.abs(ImprovedNoise.noise(p.X,p.Y,p.Z)))));
		
		if(t < 1.0){
			return (C2.mult(t)).add(C3.mult(1.0-t));
		}else{
			t -= 1.0;
			return (C1.mult(t)).add(C2.mult(1.0-t));
		}
	}
}
