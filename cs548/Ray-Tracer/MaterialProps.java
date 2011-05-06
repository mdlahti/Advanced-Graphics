
public class MaterialProps {
	public double KA; 		//ambient coefficient
	public double KD; 		//diffuse coefficient
	public double KS; 		//specular coefficient
	public double KT; 		//transparency coefficient
	public double NI; 		//index of refraction
	public double PHONG;	//phone exponent
	public Vector Color;	//color
	public Texture tex; 	// texture
	
	public MaterialProps(double ka,double kd,double ks,double kt,double ni,double phong, Texture t){
		this.KA = ka;
		this.KD = kd;
		this.KS = ks;
		this.KT = kt;
		this.NI = ni;
		this.PHONG = phong;
		this.tex = t;
	}
}
