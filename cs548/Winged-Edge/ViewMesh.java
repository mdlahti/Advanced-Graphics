import java.awt.Component;
import java.awt.Frame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.*;
import java.lang.*;
 
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GL2ES1;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLCanvas;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import javax.media.opengl.fixedfunc.GLMatrixFunc;
import javax.media.opengl.glu.GLU;
 
import com.sun.opengl.util.Animator;
 
/**
 *	@author Michael Lahti
 *	
 *	Draws a variety of Meshes based on user input.
 *  Meshes are created using a Winged Edge data structure.
 *	keyboard '0-4' = 0-4 loop subdivisions of current object.
 */
public class ViewMesh implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
    float rotateT = 0.0f;
 
	static boolean dragMouse = false;
	static double lastMouseX, lastMouseY;
	static final double M_PI = Math.PI;
	static final double RADIANS_PER_PIXEL = M_PI / 45;
	static final double DEGREES_TO_RADIANS = M_PI / 180.0;
	static final double EPSILON = .01;
	static double rho = 5.0;
	static double phi = M_PI/6;
	static double thet = M_PI/2;
	
	static final int numMeshes = 6;
	protected Mesh tetrahedron;
	protected Mesh tetrahedronSubOne;
	protected Mesh[] tetraMeshes = new Mesh[numMeshes];
	
	static final int SUB_ZERO	= 0;
	static final int SUB_ONE	= 1;
	static final int SUB_TWO	= 2;
	static final int SUB_THREE = 3;
	static final int SUB_FOUR  = 4;
	static final int SUB_FIVE  = 5;
	// default thing to draw
	static int whatToDraw = SUB_ZERO;
	static boolean shaded = false;
	
	static double lookat[] = {0.0,0.0,0.0};
	static double eye[] = {5.0,5.0,0.0};
	static double up[] = {0.0,0.0,1.0};
	static double hither = 0.2;
	static double yon = 150.0;
	
    static GLU glu = new GLU();
 
    static GLCanvas canvas = new GLCanvas();
 
    static Frame frame = new Frame("View Mesh");
 
    static Animator animator = new Animator(canvas);
 
    public void display(GLAutoDrawable gLDrawable) {
        final GL2 gl = gLDrawable.getGL().getGL2();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
		gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
		if(dragMouse){
			double sin_phi = Math.sin(phi*DEGREES_TO_RADIANS);
			eye[0] = rho*Math.cos(thet*DEGREES_TO_RADIANS)*sin_phi;
			eye[1] = rho*Math.sin(thet*DEGREES_TO_RADIANS)*sin_phi;
			eye[2] = rho*Math.cos(phi*DEGREES_TO_RADIANS);
			
			gl.glLoadIdentity();
			glu.gluLookAt(eye[0],eye[1],eye[2],
					  lookat[0],lookat[1],lookat[2],
					  up[0],up[1],up[2]);
		}
        gl.glPushMatrix();
		switch (whatToDraw) {
			case SUB_ZERO:
				drawTetrahedron(gLDrawable, tetraMeshes[0]);
				break;
			case SUB_ONE:
				drawTetrahedron(gLDrawable, tetraMeshes[1]);
				break;
			case SUB_TWO:
				drawTetrahedron(gLDrawable, tetraMeshes[2]);
				break;
			case SUB_THREE:
				drawTetrahedron(gLDrawable, tetraMeshes[3]);
				break;
			case SUB_FOUR:
				drawTetrahedron(gLDrawable, tetraMeshes[4]);
				break;
			case SUB_FIVE:
				drawTetrahedron(gLDrawable, tetraMeshes[5]);
				break;
			default:
				drawTetrahedron(gLDrawable, tetraMeshes[0]);
				break;
		}                                        
		gl.glPopMatrix();
    }
	
	public void makeTetrahedronMesh(){
		for(int i = 0; i < numMeshes; i++){
			tetraMeshes[i] = Mesh.tetrahedron();
			for(int j = 0; j < i; j++)
				tetraMeshes[i].loopSubdivide();
		}
	}
	
	public void drawTetrahedron(GLAutoDrawable gLDrawable, Mesh m){
		// tetrahedron holds Vectors: E, F, V
		// loop through each edge drawing lines from 
		// V.get(E.get(i).v) to V.get(E.get(E.get(i).next).v)
		//for(int i = 0; i < tetrahedron.E.size(); i++){
		final GL2 gl = gLDrawable.getGL().getGL2();
		if(shaded){
			for(Mesh.Face face : m.F){
				Mesh.Edge edge = m.E.get(face.e);
				gl.glBegin(GL.GL_TRIANGLES);
				for(int i = 0; i < 3; i++){
					gl.glColor3f(0.0f, 1.0f, 1.0f);
					Vec3 vert = m.V.get(edge.v).coord;
					Vec3 norm = m.V.get(edge.v).norm;
					gl.glNormal3f(norm.x,norm.y,norm.z);
					gl.glVertex3f(vert.x,vert.y,vert.z);
					edge = m.E.get(edge.next);
				}
				gl.glEnd();
			}
		}else{
			for(Mesh.Edge edge : m.E){
				if(edge.i < m.E.get(edge.sym).i){
					Vec3 v1 = m.V.get(edge.v).coord;
					Vec3 v2 = m.V.get(m.E.get(edge.next).v).coord;
					gl.glBegin(GL.GL_LINES);
					gl.glColor3f(0.0f, 1.0f, 1.0f);
					gl.glVertex3f(v1.x, v1.y, v1.z);
					gl.glVertex3f(v2.x, v2.y, v2.z);
					gl.glEnd();
				}
			}
		}
	}
	
	public void drawShadedTetra(GLAutoDrawable gLDrawable, Mesh m){
		
	}
	
    public void displayChanged(GLAutoDrawable gLDrawable, boolean modeChanged, boolean deviceChanged) {
    }
    
    public float[] AMBIENT_LIGHT = {0.3f, 0.3f, 0.3f, 1.0f};
    public float[] DIFFUSE_LIGHT = {0.0f, 1.0f, 1.0f, 1.0f};
    public float[] light_pos 	 = {5.0f, 5.0f, 5.0f};
    public float[] specReflection = {0.8f, 0.8f, 0.8f, 1.0f};
    
 
    public void init(GLAutoDrawable gLDrawable) {
        final GL2 gl = gLDrawable.getGL().getGL2();
        gl.glShadeModel(GLLightingFunc.GL_SMOOTH);
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glClearDepth(1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
        //gl.glCullFace(GL.GL_FRONT_FACE);
        gl.glColorMaterial(GL.GL_FRONT, GLLightingFunc.GL_AMBIENT_AND_DIFFUSE);
        gl.glMaterialfv(GL.GL_FRONT, GLLightingFunc.GL_SPECULAR, specReflection, 0);
        gl.glMateriali(GL.GL_FRONT, GLLightingFunc.GL_SHININESS, 56);
        gl.glHint(GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
        ((Component) gLDrawable).addKeyListener(this);
		((Component) gLDrawable).addMouseListener(this);
		((Component) gLDrawable).addMouseMotionListener(this);
		
		gl.glDisable(GL.GL_TEXTURE_2D);
		gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_AMBIENT, AMBIENT_LIGHT, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_DIFFUSE, DIFFUSE_LIGHT, 0);
		gl.glLightfv(GLLightingFunc.GL_LIGHT0, GLLightingFunc.GL_POSITION, light_pos, 0);
		gl.glEnable(GLLightingFunc.GL_LIGHT0);
		gl.glEnable(GLLightingFunc.GL_LIGHTING);
		//gl.glLightModelfv(GL2ES1.GL_LIGHT_MODEL_AMBIENT, global_ambient,0);
		
		makeTetrahedronMesh();
    }
 
    public void reshape(GLAutoDrawable gLDrawable, int x, int y, int width, int height) {
        final GL2 gl = gLDrawable.getGL().getGL2();
        //final GLU glu = new GLU();
		if (height <= 0) {
            height = 1;
        }
        float h = (float) width / (float) height;
        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(50.0f, 1.0, hither, yon);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);
        gl.glLoadIdentity();
		glu.gluLookAt(eye[0],eye[1],eye[2],
					  lookat[0],lookat[1],lookat[2],
					  up[0],up[1],up[2]);
		//System.out.println(eye[0] + " " + eye[1] + " " + eye[2]);
    }
 
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            exit();
        }else if (e.getKeyCode() == KeyEvent.VK_0) {
			whatToDraw = SUB_ZERO;
		}else if (e.getKeyCode() == KeyEvent.VK_1) {
			whatToDraw = SUB_ONE;
		}else if (e.getKeyCode() == KeyEvent.VK_2) {
			whatToDraw = SUB_TWO;
		}else if (e.getKeyCode() == KeyEvent.VK_3) {
			whatToDraw = SUB_THREE;
		}else if (e.getKeyCode() == KeyEvent.VK_4) {
			whatToDraw = SUB_FOUR;
		}else if (e.getKeyCode() == KeyEvent.VK_5) {
			whatToDraw = SUB_FIVE;
		}else if (e.getKeyCode() == KeyEvent.VK_S) {
			shaded = true;
		}else if (e.getKeyCode() == KeyEvent.VK_L) {
			shaded = false;
		}
    }
 
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	
	public void mousePressed(MouseEvent e){
		dragMouse = true;
		lastMouseX = e.getX();
		lastMouseY = e.getY();
	}
	
	public void mouseReleased(MouseEvent e){
		dragMouse = false;
	}
	
	public void mouseDragged(MouseEvent e){
		if (dragMouse) {
			int col = e.getX();
			int row = e.getY();
			double dxx = col - lastMouseX;
			double dyy = row - lastMouseY;
			dxx = dxx * RADIANS_PER_PIXEL;
			dyy = dyy * RADIANS_PER_PIXEL;
			thet -= dxx;
			phi += dyy;
			lastMouseX = col;
			lastMouseY = row;
		}
	}
 
    public static void exit() {
        animator.stop();
        frame.dispose();
        System.exit(0);
    }
 
    public static void main(String[] args) {
        canvas.addGLEventListener(new ViewMesh());
        frame.add(canvas);
        frame.setSize(640, 480);
        frame.setUndecorated(true);
        frame.setExtendedState(Frame.MAXIMIZED_BOTH);
        frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		    exit();
		}
	    });
        frame.setVisible(true);
        animator.start();
        canvas.requestFocus();
    }
 
    public void dispose(GLAutoDrawable gLDrawable) {
        // do nothing
    }
}