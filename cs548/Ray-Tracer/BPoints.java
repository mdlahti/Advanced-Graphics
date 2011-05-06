
// represents the 4 sets of control points for a Bezier surface
public class BPoints {
	public Vec4 P0,P1,P2,P3;
	
	public BPoints(Vec4 a, Vec4 b, Vec4 c, Vec4 d){
		this.P0 = a;
		this.P1 = b;
		this.P2 = c;
		this.P3 = d;
	}

	// get a specified set of control points
	public Vec4 get(int i){
		if(i == 0){
			return P0;
		}else if(i == 1){
			return P1;
		}else if(i == 2){
			return P2;
		}else{
			return P3;
		}
	}
}
