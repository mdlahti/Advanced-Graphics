import java.util.*;

/**
 * Winged Edge representation of a polygonal mesh.
 * Uses loop subdivision to subdivide the mesh.
 * Will triangulate any mesh that isn't triangular.
 * 
 * @author Michael Lahti
 * 4-30-2011
 */
public class Mesh {
	
    class Edge {
    	int i;    // index in array
    	int sym;  // symmetric edge
    	int v;    // vertex from which edge eminates
    	int f;    // face on left for CCW traversal
    	int prev; // previous edge for CCW traversal
    	int next; // next edge
    }
	
    class Face {
    	int i;     // index in array
    	int e;     // one of the face's edges
    	Vec3 norm; // the face's normal
    }
	
    class Vert {
    	int i;      // index in array
    	int e;      // one of the eminating edges
    	Vec3 coord; // coordinate of vertex
    	Vec3 norm;	// this vertex's normal
    }
	
    private class IntPair {
    	int i, j;
    	public IntPair(int i, int j) {
    		this.i = i;
    		this.j = j;
    	}
    	public boolean equals(Object other) {
    		IntPair o = (IntPair)other;
    		return o.i == i && o.j == j;
    	}
    	public int hashCode() {
    		return 23*i + j;
    	}
    };
	
    protected Vector<Edge> E;   // edges + symmetric edges
    protected Vector<Face> F;	// faces
    protected Vector<Vert> V;	// verts
    // store one of each original face's vert indices
    protected Vector<Integer> originalFaceVerts;
    protected Vector<Integer> originalFaceEdges;
    
    private int originalFaceCount;
    private int numSubdivides = 0;
	
    public Mesh(Vec3 verts[], int faces[][]) {
    	
    	V = new Vector<Vert>(verts.length);
    	for (int i = 0; i < verts.length; i++)
    		V.addElement(null);
    	F = new Vector<Face>(faces.length);
    	for (int i = 0; i < faces.length; i++)
    		F.addElement(null);
    	int numEdges = 0;
    	for (int f = 0; f < faces.length; f++)
    		numEdges += faces[f].length;
    	E = new Vector<Edge>(numEdges);
    	for (int i = 0; i < numEdges; i++)
    		E.addElement(null);
    	originalFaceVerts = new Vector<Integer>(faces.length);
    	originalFaceEdges = new Vector<Integer>(faces.length);
    	
    	Hashtable<IntPair,Edge> edgeTable = new Hashtable<IntPair,Edge>();
    	
    	int ei = 0;  // base index for group of edges for next face
    	
    	for (int f = 0; f < faces.length; f++) {
    		int[] face = faces[f];     // current face
    		final int N = face.length; // number of edges in face
    		for (int i = 0; i < N; i++) {		
    			Edge edge = new Edge();
    			edge.i = ei + i;
    			final int v = face[i];
    			final int vnext = face[(i+1)%N];
    			IntPair key = new IntPair(vnext,v);
    			Edge sym = edgeTable.get(key);
    			if (sym == null) {
    				key = new IntPair(v,vnext);
    				edgeTable.put(key,edge);
    			} else {
    				edge.sym = sym.i;
    				sym.sym = ei + i;
    			}
    			edge.v = v;
    			if (V.get(v) == null) {
    				Vert wvert = new Vert();
    				wvert.i = v;
    				wvert.e = edge.i;
    				wvert.coord = new Vec3(verts[v]);
    				V.set(v,wvert);
    			}
    			edge.f = f;
    			edge.next = ei + (i+1)%N;
    			edge.prev = ei + (i+N-1)%N;
    			E.set(edge.i,edge);
    		}
    		
    		Face wface = new Face();
    		wface.i = f;
    		wface.e = ei;
    		F.set(f,wface);
    		
    		ei += N;
    	}
    	// ensure mesh is triangular
    	triangulate();
    	// get number of original faces
    	originalFaceCount = F.size();
    	// get one of each original face's vertices
    	for(int i = 0; i < originalFaceCount; i++)
    		originalFaceVerts.add(E.get(F.get(i).e).v);
    	for(int i = 0; i < originalFaceCount; i++)
    		originalFaceEdges.add(F.get(i).e);
    	// set the normals for the mesh
    	setNormals();
    }
	
    public static Mesh tetrahedron() {
    	Vec3[]  verts = new Vec3[4];	// vertices
    	verts[0] = new Vec3( 1, 1, 1);
    	verts[1] = new Vec3(-1,-1, 1);
    	verts[2] = new Vec3(-1, 1,-1);
    	verts[3] = new Vec3( 1,-1,-1);
    	int faces[][] = {		// indices of vertices per face
			{ 0, 2, 1 },	// counter-clockwise
			{ 0, 1, 3 },
			{ 1, 2, 3 },
			{ 0, 3, 2 } };
    	return new Mesh(verts, faces);
    }
    
    public static Mesh square(){
    	Vec3[]  verts = new Vec3[8];	// vertices
    	verts[0] = new Vec3(-1, 1, 1);
    	verts[1] = new Vec3(-1,-1, 1);
    	verts[2] = new Vec3( 1,-1, 1);
    	verts[3] = new Vec3( 1, 1, 1);
    	verts[4] = new Vec3(-1, 1,-1);
    	verts[5] = new Vec3(-1,-1,-1);
    	verts[6] = new Vec3( 1,-1,-1);
    	verts[7] = new Vec3( 1, 1,-1);
    	int faces[][] = {		// indices of vertices per face
    			{ 0, 1, 2, 3 },	// counter-clockwise
    			{ 4, 7, 6, 5 },
    			{ 0, 4, 5, 1 },
    			{ 0, 3, 7, 4 },
    			{ 1, 5, 6, 2 },
    			{ 2, 6, 7, 3 } };
        	return new Mesh(verts, faces);
    }
    
    public static Mesh pyramid(){
    	Vec3[]  verts = new Vec3[5];	// vertices
    	verts[0] = new Vec3(-1, 1,-1);
    	verts[1] = new Vec3( 1, 1,-1);
    	verts[2] = new Vec3( 1,-1,-1);
    	verts[3] = new Vec3(-1,-1,-1);
    	verts[4] = new Vec3( 0, 0, 1);
    	int faces[][] = {		// indices of vertices per face
    			{ 0, 1, 3 },	// counter-clockwise
    			{ 1, 2, 3 },
    			{ 0, 4, 1 },
    			{ 3, 4, 0 },
    			{ 2, 4, 3 },
    			{ 1, 4, 2 } };
        	return new Mesh(verts, faces);
    }
    
    public void setNormals(){
    	// first get normals for all faces
    	int numFaces = F.size();
    	for(Face f : F){
    		Edge e = E.get(f.e);
    		Vec3 v1 = V.get(e.v).coord;
    		e = E.get(e.next);
    		Vec3 v2 = V.get(e.v).coord;
    		e = E.get(e.next);
    		Vec3 v3 = V.get(e.v).coord;
    		// set the normal for this face
    		Vec3 U = Vec3.sub(v2,v1);
    		Vec3 V = Vec3.sub(v3,v1);
    		float nx = U.y*V.z - U.z*V.y;
    		float ny = U.z*V.x - U.x*V.z;
    		float nz = U.x*V.y - U.y*V.x;
    		f.norm = new Vec3(nx,ny,nz);
    		f.norm.normalize();
    	}
    	// next set normals for all vertices
    	for(Vert v : V){
    		Vector<Vec3> norms = new Vector<Vec3>();
    		Edge e = E.get(v.e);
    		Edge eNext = e;//E.get(E.get(e.sym).next);
    		do{
    			// get eNext's Face's norm and store it for combining later
    			norms.add(F.get(eNext.f).norm);
    			eNext = E.get(E.get(eNext.sym).next);
    		}while(e.i != eNext.i);
    		// combine scaled norms here and set v.norm
    		float scale = 1.0f/norms.size();
    		Vec3 vNorm = new Vec3(0,0,0);
    		for(int i = 0; i < norms.size(); i++)
    			vNorm = Vec3.add(vNorm,norms.get(i));//.scale(scale));
    		v.norm = vNorm;
    		v.norm.normalize();
    	}
    }
	
    protected void splitEdge(int e, Vec3 newPos) {
    	Edge edge = E.get(e);
		Edge sym = E.get(edge.sym);
    	Edge e0 = new Edge();
    	Edge e1 = new Edge();
    	Vert v0 = new Vert();
    	// set indices of new edges + vert
    	e0.i = E.size();
    	e1.i = e0.i + 1;
    	// set up v0
    	v0.coord = newPos;
    	v0.i = V.size();
    	v0.e = e0.i;
    	V.add(v0);
    	// set up e0
    	e0.v = v0.i;
    	e0.f = edge.f;
    	e0.next = edge.next;
    	e0.prev = edge.i;
    	e0.sym = edge.sym;
    	// set up e1
    	e1.v = v0.i;
    	e1.f = sym.f;
    	e1.next = sym.next;
    	e1.prev = edge.sym;
    	e1.sym = edge.i;
		E.add(e0);
    	E.add(e1);	
    	// clean up connections with e and e.sym
    	E.get(edge.next).prev = e0.i;
    	E.get(sym.next).prev = e1.i;
    	edge.next = e0.i;
    	sym.next = e1.i;
    	sym.sym = e0.i;
    	edge.sym = e1.i;
    }
    
    protected void splitAllEdges(Hashtable<Integer,Vec3> vertPositions) {
    	// get the list of keys, use them to split every edge
    	Enumeration<Integer> keys = vertPositions.keys();
    	while(keys.hasMoreElements()){
    		int i = keys.nextElement();
    		splitEdge(i,vertPositions.get(i));
    	}
    }
	
    //
    // Build table that maps edge indices to
    // vertex positions for "odd vertices" in 
    // Loop subdivision scheme.
    //
    protected Hashtable<Integer,Vec3> oddLoopVerts() {
    	// get and store index of sym edges for each
    	// edge index we map to a vertex position
    	// so we don't replicate odd vertices
    	Hashtable<Integer,Vec3> oddVerts = new Hashtable<Integer,Vec3>();
    	for(int i = 0; i < E.size(); i++){
    		Edge edge = E.get(i);
			if(edge.i < E.get(edge.sym).i){
				// current edge indexed for this face hasn't been split yet
				// get the 4 relative vertices, then scale and combine
				Vec3 v1 = (Vec3) V.get(edge.v).coord.clone();
				Vec3 v2 = (Vec3) V.get(E.get(edge.next).v).coord.clone();
				Vec3 v3 = (Vec3) V.get(E.get(edge.prev).v).coord.clone();
				Vec3 v4 = (Vec3) V.get(E.get(E.get(edge.sym).prev).v).coord.clone();
				v1 = v1.scale(3.0f/8.0f);
				v2 = v2.scale(3.0f/8.0f);
				v3 = v3.scale(1.0f/8.0f);
				v4 = v4.scale(1.0f/8.0f);
				Vec3 finalVert = Vec3.add(v1, Vec3.add(v2, Vec3.add(v3,v4)));
				oddVerts.put(edge.i, Vec3.add(v1, Vec3.add(v2, Vec3.add(v3,v4))));
			}
    	}
    	return oddVerts;
    }
    
	
    // Build table that maps vertex indices to
    // vertex positions for "even vertices" in 
    // Loop subdivision scheme.
    //
    protected Hashtable<Integer,Vec3> evenLoopVerts() {
    	Hashtable<Integer,Vec3> evenVerts = new Hashtable<Integer,Vec3>();
    	for(int i = 0; i < V.size(); i++){
			Vector<Vec3> adjVerts = new Vector<Vec3>();
    		Vert v = V.get(i);
    		Vec3 vCoord = (Vec3) v.coord.clone();
    		// grow a list of adjacent verts, use the 
    		// number of adjacent verts to calculate B
    		// then scale and combine adjacent verts
    		Edge e = E.get(v.e);
    		Edge nextEdge = e;
    		do{
    			nextEdge = E.get(nextEdge.next);
    			adjVerts.add((Vec3) V.get(nextEdge.v).coord.clone());
    			nextEdge = E.get(E.get(nextEdge.next).sym);
    		}while(nextEdge.i != e.i);
    		float n = adjVerts.size();
    		float B;
    		if(n > 3)
    			B = 3.0f/(8*n);
    		else
    			B = 3.0f/16.0f;
    		// scale vCoord and the adjacent verts
    		// and combine them
    		vCoord = vCoord.scale(1-n*B);
    		for(int j = 0; j < adjVerts.size(); j++)
    			vCoord = Vec3.add(vCoord,adjVerts.get(j).scale(B));
    		// vCoord is now our new even loop vert Vec3
    		evenVerts.put(v.i, vCoord);
    	}
    	return evenVerts;
    }
    
    protected void adjustEvenVerts(Hashtable<Integer,Vec3> evenVerts){
    	for(int i = 0; i < V.size(); i++){
    		Vert v = V.get(i);
    		v.coord = evenVerts.get(v.i);
    	}
    }
	
    //
    // Triangulate a single face
    //
    // After the edges of a triangle have been split,    
    // we need to connect the new (odd) vertices so each      
    // face is split into four faces. The figure below shows  
    // one step of that process by adding an edge connecting  
    // vertices v1 and v3. Once this is complete we will      
    // update f->e = enew and lather, rinse, repeat.          
    // We're done when e = e->next->next->next; this          
    // condition also prevents us from triangulating a        
    // face that has already been triangulated.                     
    //
    // Interestingly enough, this routine will triangulate      
    // any polygon whether the edges have been split or not.    
    //                                                          
    //    * : even vertex  (old vertices from before edge splitt
    //    o : odd vertex (created from edge split)              
    //    f : original face                                     
    //    e = f->e : edge emanating from even vertex            
    //    enew, esym : newly added edge and its symmetric edge  
    //    fnew : newly added face                               
    //
    //                     v2                                   
    //                      *                                   
    //                     / \                      . avoiding  
    //                    /   \                     . backslash 
    //                e2 /fnew \ e1 = e->next       . as        
    //                  / esym  \                   . last      
    //              v3 o.........o v1 = e1->v       . character 
    //                /   enew    \                 . on        
    //           e3  /             \ e = f->e       . line      
    //              /       f       \               .           
    //             /                 \              .           
    //         v4 *---------o---------* v0 = e->v               
    //                     v5                                   
    public void triangulate(Face f) {
    	Edge e = E.get(f.e);
    	Edge e1 = E.get(e.next);
    	// check to make sure this face isn't already triangular
    	if(e.i != E.get(E.get(e.next).next).next){
    		// done when e == e->next->next->next
    		do{
    			Edge e2 = E.get(e1.next);
    			Edge e3 = E.get(e2.next);
    			// Edge = {i, sym, v, f, prev, next}
    			Edge enew = new Edge();
    			enew.i = E.size();
    			Edge esym = new Edge();
    			esym.i = enew.i + 1;
    			// Face = {i, e}
    			Face fnew = new Face();
    			fnew.i = F.size();
    			fnew.e = e1.i;
    			// set up esym
    			esym.sym = enew.i;
    			esym.v = e3.v;
    			esym.f = fnew.i;
    			esym.prev = e2.i;
    			esym.next = e1.i;
    			// set up enew
    			enew.sym = esym.i;
    			enew.v = e1.v;
    			enew.f = f.i;
    			enew.prev = e1.prev;
    			enew.next = e2.next;
    			E.add(enew);
    			E.add(esym);
    			F.add(fnew);
    			// clean up loose ends
    			// e1.prev.next, e2.next.prev, e1.prev, e2.next, e1.f, e2.f
    			E.get(e1.prev).next = enew.i;
    			e3.prev = enew.i;
    			e1.prev = esym.i;
    			e2.next = esym.i;
    			e1.f = fnew.i;
    			e2.f = fnew.i;
    			f.e = enew.i;
    			// set e1 and e2 for next step
    			e1 = e3;
    			//e2 = E.get(e3.next);
    		}while(e.i != E.get(E.get(e.next).next).next);
    	}
    }
    
	
    //
    // Triangulate all faces
    //
    public void triangulate() {
		int numFaces = F.size();
    	for(int i = 0; i < numFaces; i++){
    		triangulate(F.get(i));
    	}
    }
	
    public void loopSubdivide() {
    	// (1) get odd vertex map
    	Hashtable<Integer,Vec3> oddVerts = oddLoopVerts();
    	// (2) get even vertex map
    	Hashtable<Integer,Vec3> evenVerts = evenLoopVerts();
    	// (3) adjust even vertices
    	adjustEvenVerts(evenVerts);
    	// (4) split all edges using odd vertex map
    	splitAllEdges(oddVerts);
    	// (5) triangulate
    	triangulate();
    	// (6) reset normals
    	setNormals();
    	// increment numSubdivides
    	numSubdivides++;
    }
    
    public int getOriginalFaceCount(){
    	return originalFaceCount;
    }
    
    public int getNumSubdivides(){
    	return numSubdivides;
    }
}
