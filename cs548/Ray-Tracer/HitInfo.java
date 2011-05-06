
public class HitInfo {
	public double t;
	public Vector Color;
	public Vector N;
	public MaterialProps mp;
	
	// t, normal, materials, color
	public HitInfo(double T, Vector n, MaterialProps m, Vector c){
		this.t = T;
		this.N = n;
		this.mp = m;
		this.Color = c;
	}
}
