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
    public Vec3 scale(float s) {
    	x *= s; y *= s; z *= s;
    	return this;
    }
};