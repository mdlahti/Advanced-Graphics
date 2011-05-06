
public class SimpleTexture extends Texture{

	private Vector C;
	
	public SimpleTexture(double cr, double cg, double cb){
		C = new Vector(cr,cg,cb);
	}
	
	public Vector getColor(Vector p){
		return C;
	}
}
