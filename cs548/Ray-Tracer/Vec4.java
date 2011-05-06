
//class to hold 4 3-dimensional points
public class Vec4 {
	public Vector A,B,C,D;
	
	public Vec4(){
		
	}
	
	public Vec4(Vector a, Vector b, Vector c, Vector d){
		this.A = a;
		this.B = b;
		this.C = c;
		this.D = d;
	}
	
	// set the specified sub vector to v
	public void set(int i, Vector v){
		if(i == 0){
			this.A = v;
		}else if(i == 1){
			this.B = v;
		}else if(i == 2){
			this.C = v;
		}else if(i == 3){
			this.D = v;
		}
	}
	
	// add v to all 4 sub vectors
	public void add(Vector v){
		this.A = this.A.add(v);
		this.B = this.B.add(v);
		this.C = this.C.add(v);
		this.D = this.D.add(v);
	}
	
	// scale all 4 sub vectors by s
	public void scale(double s){
		this.A = this.A.mult(s);
		this.B = this.B.mult(s);
		this.C = this.C.mult(s);
		this.D = this.D.mult(s);
	}
}
