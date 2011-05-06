
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.io.*;

/**
 * RayTracer for producing .ppm images of specified scenes.
 * 
 * @author Michael Lahti
 * 
 * 4-30-11
 *
 */
public class RayTracer {
	static int W,H;
	static double w, h;
	static Vector e,p,u,R,U,D;
	static ArrayList<RayObject> Objects;
	static Vector backGroundColor;
	static Vector Light1;
	static Vector Light1intensity;
	static Vector Lamb;
	static String filename;
	static double tmin = 0.001, tmax = 100000000;
	
	public static Vector ray(Vector o, Vector d, int k, double ni){
		double t = -1.0;
		Vector color;
		Vector Rr = null;
		boolean refracts = false;
		boolean reflects = true;
		double newNI = 1; //NI of the hit object (1 for air)
		// loop through arraylist of objects here to find nearest collision
		int total = Objects.size();
		HitInfo info = null; // info should end up with the nearest collision's hit info
		for(int index=0; index < total; index++){
			HitInfo tempInfo = Objects.get(index).rayHit(o, d, tmin, tmax);
			if(tempInfo.t > 0){
				if(t < 0){
					t = tempInfo.t;
					info = tempInfo;
				}else if(tempInfo.t < t && tempInfo.t > 0){
					t = tempInfo.t;
					info = tempInfo;
				}
			}
		}
		Vector P = null, Reflect = null;
		if(t > 0){
			P = o.add(d.mult(t));
			Vector N = info.N;
			MaterialProps mats = info.mp;
			newNI = mats.NI;
			Vector L = Light1.sub(P);
			L.unit();
			Vector V = new Vector(-d.X,-d.Y,-d.Z);
			if(d.dot(N) > 0){  //exiting object
				newNI = 1.0;
				N = N.mult(-1.0);
			}
			Reflect = (N.mult(2*V.dot(N))).sub(V);
			Reflect.unit();
			Vector Half = L.add(V);
			Half.unit();
			Vector I = Lamb.mult(mats.KA);
			boolean inShadow = false, lightShadow = false;
			Vector PL = Light1.sub(P);
			double plLength = PL.length();
			// check if object is in shadow
			for(int index=0; index < total; index++){
				if(Objects.get(index).shadowHit(P, L, 0.09, plLength) ){
					inShadow = true;
					// check if shadowed by transparent object
					if(Objects.get(index).materials().KT >= 1.0){
						lightShadow = true;
					}
					break;
				}
			}
			
			if(!inShadow){
				//not in shadow so add diffuse+specular intensities
				double specular = mats.KS*Math.pow(Math.max(0.0, Half.dot(N)),mats.PHONG);
				I = I.add(Light1intensity.mult(Math.max(0.0,mats.KD*N.dot(L)) + specular));
				if(mats.KT >= 1.0){
					I = new Vector(0.9,0.9,0.9);
				}
			}else if(lightShadow){
				I = I.add(new Vector(0.175,0.175,0.175));
			}
			//get object's color
			color = info.Color;
			color = color.multV(I);
			// handle possible refraction
			if(mats.KT > 0){
				refracts = true;
				//calculate new d here for recursive call
				//n = ni, nt = newNI
				double nn = ni/newNI;
				double c1 = -N.dot(d);
				double c2 = Math.sqrt(1.0 - nn*nn*(1.0 - c1*c1));
				Rr = d.mult(nn).add(N.mult(nn*c1-c2));
				Rr.unit();
				
			}
		}else{
			//t < 0
			//no hit, background color
			color = backGroundColor;
		}
		//have color, if t>0 and k>0 compute color+refraction+reflection IF object refracts and reflects
			//if object just reflects, compute color + reflection
			//if object just refracts, compute color + refraction
			//if object does neither, just return color
		//if t or k < 0 then just return color
		if(k > 0){
			if(t > 0){
				if(reflects && refracts){
					k--;
					Vector color2 = ray(P,Reflect,k,newNI);
					Vector color3 = ray(P,Rr,k,newNI);
					return ((color.mult(0.1)).add(color2.mult(0.3))).add(color3.mult(0.6));
					//return (color.mult(0.1)).add(ray(P,Reflect,k,newNI).mult(0.3)).add(ray(P,Rr,k,newNI).mult(0.6));
				}else if(reflects){
					return (color.mult(0.7)).add(ray(P,Reflect,--k,newNI).mult(0.3));
				}else if(refracts){
					return (color.mult(0.7)).add(ray(o,d,--k,newNI).mult(0.3));
				}else{
					//no reflection or refraction, just return color
					return color;
				}
			}
			//set k = 0 (no collision anyways) and return color
			k = 0;
			return color;
		}else{
			//end of recursion, just return color
			return color;
		}
	}
	
	public static void rayCast() throws IOException{
		int numRecursions = 1;
		Vector finalColor = null;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
		bw.write("P3");
		bw.newLine();
		bw.write(String.valueOf(W));
		bw.write(" ");
		bw.write(String.valueOf(H));
		bw.newLine();
		bw.write(String.valueOf(255));
		bw.newLine();
		
		int numSamples = 1;
		int red=0, green=0, blue=0;
		
		for(int j = 0; j < W; j++){
			for(int i = 0; i < H; i++){
				Vector[] samples = new Vector[numSamples];
				double x = i*(w/(W-1.0)) - w/2.0;
				double y = j*(-h/(H-1.0)) + h/2.0;
				
				if(numSamples == 1){
					Vector o = new Vector(e.X,e.Y,e.Z);
					Vector d = (R.mult(x)).add(U.mult(y)).add(D);
					d.unit();
					samples[0] = ray(o,d,numRecursions,1.0);
				}else if(numSamples == 4){
					// use random stratified sampling
					Random rnd = new Random();
					double xleft = (i-0.5)*(w/(W-1.0)) - w/2.0;
					double ytop = (j+0.5)*(-h/(H-1.0)) + h/2.0;
					double xdiff = Math.abs(xleft - x);
					double ydiff = Math.abs(ytop - y);
					int index = 0;
					// get top left, bottom left, top right, then bottom right samples
					for(int k = 0; k < 2; k++, xleft += xdiff){
						double YT = ytop;
						for(int l = 0; l < 2; l++, index++, YT += ydiff){
							double xrnd = rnd.nextDouble()*xdiff;
							double yrnd = rnd.nextDouble()*ydiff;
							double X = xleft + xrnd;
							double Y = YT - yrnd;
							Vector o = new Vector(e.X,e.Y,e.Z);
							Vector d = (R.mult(X)).add(U.mult(Y)).add(D);
							d.unit();
							samples[index] = ray(o,d,numRecursions,1.0);
						}
					}
				}
				// blend samples
				finalColor = new Vector(0,0,0);
				for(int k = 0; k < numSamples; k++){
					finalColor = finalColor.add(samples[k].mult(1.0/numSamples));
				}
				red =   (int) (finalColor.X*256);
				green = (int) (finalColor.Y*256);
				blue =  (int) (finalColor.Z*256);
				
				if(red > 255)
					red = 255;
				if(green > 255)
					green = 255;
				if(blue > 255)
					blue = 255;
				
				bw.write(String.valueOf(red));
				bw.write(" ");
				bw.write(String.valueOf(green));
				bw.write(" ");
				bw.write(String.valueOf(blue));
				bw.write(" ");
			}
			bw.newLine();
		}
		bw.close();
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO find reason behind symmetric dead pixels where ray goes 
		// straight through teapot without detecting collision, debug image texturing
		double r = 2.0;
		filename = args[0];
		Objects = new ArrayList<RayObject>();
		MaterialProps mats;
		
		/*//2-D image textured sphere
		mats = new MaterialProps(0.1,0.6,0.3,0.0,1.52,10,0.1,0.2,0.6); //blueish sphere
		Objects.add(new ImagedSphere(r*Math.sqrt(3)/2,-r/2,r,r,mats,"test3.ppm"));*/
		
		
		// TEAPOT OBJECTS
		mats = new MaterialProps(0.1,0.3,0.6,0.0,1.52,20,new SimpleTexture(.9,.9,.1)); //yellowish plane
		Objects.add(new Plane(0,0,0,0,1,0,mats));  
		
		mats = new MaterialProps(0.1,0.7,0.2,0.0,1.52,10,new MarbleTexture(
														new Vector(0,.8,.9),
														new Vector(0,.4,.9),
														new Vector(0,.1,.3), 0.15)); //teapot
		Objects.add(new Teapot(new Vector(0,0,0),2.0,mats));
		
		/*
		// STACKED BALLS OBJECTS
		mats = new MaterialProps(0.1,0.3,0.6,0.0,1.52,20,new SimpleTexture(.9,.9,.1)); //yellowish plane
		Objects.add(new Plane(0,0,0,0,0,1,mats));
		
		mats = new MaterialProps(0.1,0.6,0.3,0.0,1.52,10,new SimpleTexture(0.6,0.2,0.1)); //redish sphere
		Objects.add(new Sphere(-r*Math.sqrt(3)/2,-r/2,r,r,mats));
		
		mats = new MaterialProps(0.1,0.6,0.3,0.0,1.52,10,new SimpleTexture(0.1,0.2,0.6)); //blueish sphere
		Objects.add(new Sphere(r*Math.sqrt(3)/2,-r/2,r,r,mats));
		
		mats = new MaterialProps(0.1,0.6,0.3,0.0,1.52,10,new SimpleTexture(0.1,0.8,0.6)); //purpleish sphere
		Objects.add(new Sphere(0,r,r,r,mats));
		
		mats = new MaterialProps(0.0,0.0,1.0,1.0,1.52,10,new SimpleTexture(0.8,0.8,0.9)); //whiteish sphere
		Objects.add(new Sphere(0,0,r*(2*Math.sqrt(2.0/3.0)+1),r,mats));
		*/
		/*
		mats = new MaterialProps(0.15, 0.656667, 0.2, 0.0, 1.52, 10.0, 0.9, 0.8, 0.1);
		Vec4 P0 = new Vec4(new Vector(-3,-3,4),new Vector(-1,-3,0),new Vector(1,-3,0),new Vector(3,-3,-2));
		Vec4 P1 = new Vec4(new Vector(-3,-1,0),new Vector(-1,-1,3),new Vector(1,-1,0),new Vector(3,-1,0));
		Vec4 P2 = new Vec4(new Vector(-3,1,0),new Vector(-1,1,0),new Vector(1,1,0),new Vector(3,1,1));
		Vec4 P3 = new Vec4(new Vector(-3,3,2),new Vector(-1,3,1),new Vector(1,3,0),new Vector(3,3,-1));
		Objects.add(new Bezier(new BPoints(P0,P1,P2,P3),mats));
		
		mats = new MaterialProps(0.0,0.3,0.7,0.0,1.52,100,0.9,0.8,0.7); // plane
		Objects.add(new Plane(0,0,-6,0,0,1,mats));
		*/
		backGroundColor = new Vector(0.0,0.1,0.6);
		W = 512;
		H = 512;
		/*
		// stacked balls coordinates
		e = new Vector(2.5*r,-2.5*r,3.5*r);
		p = new Vector(0.0,0.0,1.5*r);
		u = new Vector(0.0,0.0,1.0);
		*/
		// teapot coordinates
		e = new Vector(0.0,7.5,8.0);
		p = new Vector(0.0,3.0,0.0);
		u = new Vector(0.0,1.0,0.0);
		
		double FoV = 80.0;
		double aspectRatio = 1.0;
		
		D = p.sub(e);
		D.unit();
		R = D.cross(u);
		R.unit();
		U = R.cross(D);
		h = 2*(Math.tan(FoV*Math.PI/180/2.0));
		w = (h*aspectRatio);
		Light1 = new Vector(0.0,10.0,10.0);
		Light1intensity = new Vector(1.0,1.0,1.0);
		Lamb = new Vector(0.1,0.1,0.1);
		
		rayCast();
	}
}
