package unsw.graphics.world;



import java.awt.Color;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import unsw.graphics.CoordFrame2D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Point3DBuffer;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment HeightMap 
 *
 * @author malcolmr
 */
public class Terrain {

    private int width;
    private int depth;
    private float[][] altitudes;
    private List<Tree> trees;
    private List<Road> roads;
    private Vector3 sunlight;
    
    private TriangleMesh ground;
    private TriangleMesh treeMesh;
    private Texture texture;
    
//    private Point3DBuffer vertexBuffer;
//    private IntBuffer indicesBuffer;
//    private int verticesName;
//    private int indicesName;

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth, Vector3 sunlight) {
        this.width = width;
        this.depth = depth;
        altitudes = new float[width][depth];
        trees = new ArrayList<Tree>();
        roads = new ArrayList<Road>();
        this.sunlight = sunlight;
    }

    public void Init(GL3 gl){
    	
    	List<Point3D> vertexList = new ArrayList<Point3D>();
    	List<Point2D> texList = new ArrayList<Point2D>();
    	List<Integer> IntList = new ArrayList<Integer>();
    	
    	for(int z = 0; z < depth ;z++){
    		
    		for(int x = 0; x < width ;x++){
        		
        		vertexList.add(new Point3D(x,altitudes[z][x],z));
        		texList.add(new Point2D((float)x/(width-1),(float)z/(depth-1)));
        		
        		if(z < depth-1){
        			
        			if(x < width-1){
        				
        				IntList.add((z*width)+x);
        				IntList.add(((z+1)*width)+x+1);
        				IntList.add((z*width)+x+1);
        			}
        			
        		}
        		if(z > 0){
        			
        			if(x < width-1){
        				
        				IntList.add((z*width)+x);
        				IntList.add((z*width)+x+1);
        				IntList.add(((z-1)*width)+x);
        			}
        			
        		}
        	}
    		
    	}
    	
    	ground = new TriangleMesh(vertexList, IntList,true, texList);
    	ground.init(gl);
    	texture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
    	
    	try {
			treeMesh = new TriangleMesh("res/models/tree.ply",true,true);
			treeMesh.init(gl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    }
    
    public void draw(GL3 gl, CoordFrame3D frame){
    	
    	Shader.setInt(gl, "tex", 0);
        
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
    	
    	Shader.setPenColor(gl, Color.WHITE);
    	ground.draw(gl,frame);
    	
    	
    	
    	if(treeMesh != null){
    		
    		for(int i = 0; i < trees.size(); i++){
    			
    			Point3D temp = trees.get(i).getPosition();
    			treeMesh.draw(gl, frame.translate(temp));
    			
    		}
    		
    	}
    	
    }
    
    public List<Tree> trees() {
        return trees;
    }

    public List<Road> roads() {
        return roads;
    }

    public Vector3 getSunlight() {
        return sunlight;
    }

    /**
     * Set the sunlight direction. 
     * 
     * Note: the sun should be treated as a directional light, without a position
     * 
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        sunlight = new Vector3(dx, dy, dz);      
    }

    /**
     * Get the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return altitudes[x][z];
    }

    /**
     * Set the altitude at a grid point
     * 
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, float h) {
        altitudes[x][z] = h;
    }

    /**
     * Get the altitude at an arbitrary point. 
     * Non-integer points should be interpolated from neighbouring grid points
     * 
     * @param x
     * @param z
     * @return
     */
    public float altitude(float x, float z) {
        float altitude = 0;
        
        double xF = Math.floor(x);
        double xC = Math.ceil(x);
        
        double zF = Math.floor(z);
        double zC = Math.ceil(z);
        
        double xDiff = x-xF;
        double zDiff = z-zF;
        
        float interpBot = (float)(getGridAltitude((int)xC,(int)zC) * xDiff)
        		+(float)(getGridAltitude((int)xF,(int)zC) * (1-xDiff));
        
        float interpTop = (float)(getGridAltitude((int)xC,(int)zF) * xDiff)
        		+(float)(getGridAltitude((int)xF,(int)zF) * (1-xDiff));
        
        altitude = (float)(interpTop * (1-zDiff) + interpBot * zDiff);
        
        // TODO: Implement this
        
        System.out.println("X = " + x + ", Z =  "+ z + ", Alt = " + altitude);
        
        return altitude;
    }

    /**
     * Add a tree at the specified (x,z) point. 
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     * 
     * @param x
     * @param z
     */
    public void addTree(float x, float z) {
        float y = altitude(x, z);
        Tree tree = new Tree(x, y, z);
        trees.add(tree);
    }


    /**
     * Add a road. 
     * 
     * @param x
     * @param z
     */
    public void addRoad(float width, List<Point2D> spine) {
        Road road = new Road(width, spine);
        roads.add(road);        
    }

}
