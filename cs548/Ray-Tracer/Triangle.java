
public class Triangle {
	MaterialProps mats;
	public Vector P0,P1,P2;
	
	// P2
	// | \
	// |  \
	// |   \
	// P0---P1
	
	public Triangle(Vector a, Vector b, Vector c, MaterialProps m){
		this.P0 = a;
		this.P1 = b;
		this.P2 = c;
		this.mats = m;
	}
	
	public HitInfo rayHit(Vector o, Vector d, double tmin, double tmax){
		double t;
		double A = P0.X - P1.X;
		double B = P0.Y - P1.Y;
		double C = P0.Z - P1.Z;
		
		double D = P0.X - P2.X;
		double E = P0.Y - P2.Y;
		double F = P0.Z - P2.Z;
		
		double G = d.X;
		double H = d.Y;
		double I = d.Z;
		
		double J = P0.X - o.X;
		double K = P0.Y - o.Y;
		double L = P0.Z - o.Z;
		
		double EIHF = E*I - H*F;
		double GFDI = G*F - D*I;
		double DHEG = D*H - E*G;
		
		double denom = (A*EIHF + B*GFDI + C*DHEG);
		double beta = (J*EIHF + K*GFDI + L*DHEG) / denom;
		
		if(beta <= 0.0 || beta >= 1.0)
			return (new HitInfo(-1.0,null,null,null));
		
		double AKJB = A*K - J*B;
		double JCAL = J*C - A*L;
		double BLKC = B*L - K*C;
		
		double gamma = (I*AKJB + H*JCAL + G*BLKC) / denom;
		if(gamma <= 0.0 || (beta + gamma >= 1.0))
			return (new HitInfo(-1.0,null,null,null));
		
		t = -(F*AKJB + E*JCAL + D*BLKC) / denom;
		if(t >= tmin && t <= tmax){
			Vector N = (P1.sub(P0)).cross((P2.sub(P0)));
			N.unit();
			return (new HitInfo(t,N,this.mats,mats.tex.getColor(o.add(d.mult(t)))));
		}
		return (new HitInfo(-1.0,null,null,null));
	}
}
