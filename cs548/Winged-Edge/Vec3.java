public class Vec3 {
    public float x, y, z;
    public Vec3(float x, float y, float z) {
    	this.x = x;
    	this.y = y;
    	this.z = z;
    }
    public Vec3(Vec3 v) {this(v.x,v.y,v.z);}
    public Object clone() {return new Vec3(this);}
    public static Vec3 add(Vec3 a, Vec3 b) {
    	return new Vec3(a.x+b.x, a.y+b.y, a.z+b.z);
    }
    public static Vec3 sub(Vec3 a, Vec3 b) {
    	return new Vec3(a.x-b.x, a.y-b.y, a.z-b.z);
    }
    public void normalize(){
    	float len = (float)Math.sqrt(this.x*this.x+this.y*this.y+this.z*this.z);
		this.x = this.x / len;
		this.y = this.y / len;
		this.z = this.z / len;
    }
    public Vec3 scale(float s) {
    	x *= s; y *= s; z *= s;
    	return this;
    }
};