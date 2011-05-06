
public class Vector {
	public double X;
	public double Y;
	public double Z;
	
	public Vector(){
		
	}
	
	// create a vector from 3 values
	public Vector(double x, double y, double z){
		this.X = x;
		this.Y = y;
		this.Z = z;
	}
	
	// create a vector from a double array
	public Vector(double[] d){
		this.X = d[0];
		this.Y = d[1];
		this.Z = d[2];
	}
	
	void setX(double x){
		this.X = x;
	}
	
	void setY(double y){
		this.Y = y;
	}
	
	void setZ(double z){
		this.Z = z;
	}
	
	// Return the dot product of this vector and another one
	double dot(Vector other){
		return this.X*other.X + this.Y*other.Y + this.Z*other.Z;
	}
	
	// Return the cross product of this vector and another one
	Vector cross(Vector other){
		Vector temp = new Vector();
		temp.X = this.Y*other.Z - this.Z*other.Y;
		temp.Y = this.Z*other.X - this.X*other.Z;
		temp.Z = this.X*other.Y - this.Y*other.X;
		return temp;
	}
	
	// Multiply this vector by a value 'm'
	Vector mult(double m){
		Vector temp = new Vector();
		temp.X = this.X * m;
		temp.Y = this.Y * m;
		temp.Z = this.Z * m;
		return temp;
	}
	
	// Add another vector to this one
	Vector add(Vector other){
		Vector temp = new Vector();
		temp.X = this.X + other.X;
		temp.Y = this.Y + other.Y;
		temp.Z = this.Z + other.Z;
		return temp;
	}
	
	// Subtract another vector from this one
	Vector sub(Vector other){
		Vector temp = new Vector();
		temp.X = this.X - other.X;
		temp.Y = this.Y - other.Y;
		temp.Z = this.Z - other.Z;
		return temp;
	}
	
	// Get the length of this vector
	double length(){
		return Math.sqrt(this.X*this.X+this.Y*this.Y+this.Z*this.Z);
	}
	
	// Make this Vector a unit vector
	void unit(){
		double len = Math.sqrt(this.X*this.X+this.Y*this.Y+this.Z*this.Z);
		this.X = this.X / len;
		this.Y = this.Y / len;
		this.Z = this.Z / len;
	}
	
	// Return -1*vector
	Vector neg(){
		Vector temp = new Vector(-this.X, -this.Y, -this.Z);
		return temp;
	}
	
	// Divide vector by a single value: x/r,y/r,z/r
	Vector div(double r){
		Vector temp = new Vector(this.X/r,this.Y/r,this.Z/r);
		return temp;
	}
	
	// Multiply one vector by another: x*x,y*y,z*z
	Vector multV(Vector other){
		return (new Vector(this.X*other.X,this.Y*other.Y,this.Z*other.Z));
	}
}
