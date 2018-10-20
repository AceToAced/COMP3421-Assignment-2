package unsw.graphics.world;



import java.awt.Color;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;

import unsw.graphics.*;
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
    private List<Pond> ponds;
    
    private TriangleMesh ground;
    private TriangleMesh treeMesh;
    private Texture texture;
    private Texture roadTexture;
    private List<TriangleMesh> roadMeshes;
    
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
        ponds = new ArrayList<Pond>();
        this.sunlight = sunlight;
    }

    public void Init(GL3 gl){
    	
    	List<Point3D> vertexList = new ArrayList<Point3D>();
    	List<Point2D> texList = new ArrayList<Point2D>();
    	List<Integer> indicesList = new ArrayList<Integer>();
    	
    	for(int z = 0; z < depth ;z++){
    		
    		for(int x = 0; x < width ;x++){
        		
        		vertexList.add(new Point3D(x,altitudes[x][z],z));
        		texList.add(new Point2D((float)x/(width-1),(float)z/(depth-1)));
        		
        		if(z < depth-1){
        			
        			if(x < width-1){
        				
        				indicesList.add((z*width)+x);
        				indicesList.add(((z+1)*width)+x);
        				indicesList.add((z*width)+x+1);
        			}
        			
        		}
        		if(z > 0){
        			
        			if(x < width-1){
        				
        				indicesList.add((z*width)+x);
        				indicesList.add((z*width)+x+1);
        				indicesList.add(((z-1)*width)+x+1);
        			}
        			
        		}
        	}
    		
    	}
    	
    	ground = new TriangleMesh(vertexList, indicesList,true, texList);
    	ground.init(gl);
    	texture = new Texture(gl, "res/textures/grass.bmp", "bmp", false);
    	roadTexture = new Texture(gl, "res/textures/rock.bmp", "bmp", false);
    	try {
			treeMesh = new TriangleMesh("res/models/tree.ply",true,true);
			treeMesh.init(gl);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	this.roadMeshes = new ArrayList<TriangleMesh>();
    	generateRoads(gl);
    	
    	for (TriangleMesh mesh : this.roadMeshes) {
    		mesh.init(gl);
        }
    	
    	for(Pond pond: ponds){
    		pond.Init(gl);
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
    			treeMesh.draw(gl, frame.translate(temp).translate(0.0f, 5.0259f, 0.6f));

    		}
    		
    	}

        gl.glBindTexture(GL.GL_TEXTURE_2D, roadTexture.getId());

        //fix z-fighting
        gl.glEnable(GL3.GL_POLYGON_OFFSET_FILL);
        gl.glPolygonOffset(-1.0f, -1.0f); 
    	
        // Draw roads
        if (roadMeshes != null) {
        	for (TriangleMesh mesh : roadMeshes) {
                mesh.draw(gl, frame);
            }
        }
        
        
    	for(Pond pond: ponds){
    		pond.draw(gl, frame);
    	}
    	
    	gl.glDisable(GL3.GL_POLYGON_OFFSET_FILL);
    	
    }

    private void generateRoads(GL3 gl) {

    	for (Road road : roads) {
            List<Point3D> vertices = new ArrayList<>();
            Point2D startPoint = road.point(0);
            float altitude = altitude(startPoint.getX(), startPoint.getY());
            int roadSize = road.size();
            float dt = (1.0f/roadSize)/10;
            int end = 0;
            if (roadSize == 1) {
            	end = 10;
            } else if (roadSize == 2) {
            	end = 40;
            }
            for(int inc = 0; inc < end; inc++){
                float t = inc*dt;
                // The origin
                Point2D origin2D = road.point(t);
                Point3D origin = new Point3D(origin2D.getX(), altitude, origin2D.getY());
                // Compute the frenet frame
                Point2D tangent = road.pointDerivative(t);
                System.out.println("tangent at t= " + Float.toString(t) + " is " + tangent.toString());
                float k1 = tangent.getX();
                float k2 = tangent.getY();
                Vector3 k = new Vector3(k1, 0, k2).normalize();
                Vector3 i = new Vector3(k2, 0, -k1).normalize();
                Vector3 j = k.cross(i);
                float[] values = new float[] {
                        i.getX(), i.getY(), i.getZ(), 0, // i
                        j.getX(), j.getY(), j.getZ(), 0, // j
                        k.getX(), k.getY(), k.getZ(), 0, // k
                        origin.getX(), origin.getY(), origin.getZ(), 1  // phi
                };
                Matrix4 frenetFrame = new Matrix4(values);
                float roadWidth = (float) road.width()/2f;
                Vector4 l1 = new Vector4(roadWidth, 0, 0, 1);
                Vector4 l2 =  new Vector4(-roadWidth, 0, 0, 1);;
                Point3D ml1 = frenetFrame.multiply(l1).asPoint3D();
                Point3D ml2 = frenetFrame.multiply(l2).asPoint3D();
                vertices.add(ml1);
                vertices.add(ml2);
            }
            List<Integer> indices = new ArrayList<>();
            List<Point2D> textureList = new ArrayList<>();
            for (int i = 0; i <= vertices.size() - 4; i += 4) {
                // First triangle of quad
                indices.add(i);
                indices.add(i+1);
                indices.add(i+2);
                textureList.add(new Point2D(0,0));
                textureList.add(new Point2D(1,0));
                textureList.add(new Point2D(1,1));
                // Second triangle of quad
                indices.add(i+1);
                indices.add(i+3);
                indices.add(i+2);
                textureList.add(new Point2D(0,0));
                textureList.add(new Point2D(1,0));
                textureList.add(new Point2D(1,1));
                
                if (i != vertices.size() -4) {
                    // Third triangle
                    indices.add(i+2);
                    indices.add(i+3);
                    indices.add(i+4);
                    textureList.add(new Point2D(0,0));
                    textureList.add(new Point2D(1,0));
                    textureList.add(new Point2D(1,1));
                }
                if (i <= vertices.size() -5) {
                 // Fourth triangle
                  indices.add(i+3);
                  indices.add(i+5);
                  indices.add(i+4);
	              textureList.add(new Point2D(0,0));
	              textureList.add(new Point2D(1,0));
	              textureList.add(new Point2D(1,1));
                }
            }
            TriangleMesh mesh = new TriangleMesh(vertices, indices, true, textureList);
            this.roadMeshes.add(mesh);
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
        
        if(z > depth-1 || z < 0.0f || x > width-1 || x < 0.0f){
        	//System.out.println("OUT RANGE Y = 0 , x:" + x + " z:" + z);
        	return altitude;
        }
        
        double xF = Math.floor(x);
        double xC = Math.ceil(x);
        
        double zF = Math.floor(z);
        double zC = Math.ceil(z);
        
        double xDiff = x-xF;
        double zDiff = z-zF;

        if(zDiff + xDiff == 1.0f){
        	
        	//System.out.print("4: ");
        	
        	altitude = (float)(getGridAltitude((int)xC,(int)zF) * xDiff)
        			+(float)(getGridAltitude((int)xF,(int)zC) * (1-xDiff));
        }else if(zF == zC || xC == xF){
        	
        	if(zF == zC && xC == xF){
        		
        		altitude = (float)getGridAltitude((int)xF,(int)zC);
        		//System.out.println("ON A POINT Y = " + altitude);
        		return altitude;
        	}
        	
        	//System.out.print("3: ");
        	
        	if(zF == zC){
        		
        		altitude = (float) (((xDiff) * getGridAltitude((int)xC,(int)zC)) 
        				+ ((1-xDiff) * getGridAltitude((int)xF,(int)zC)));
        		
        	}else{
        		
        		altitude = (float) (((zDiff) * getGridAltitude((int)xC,(int)zC)) 
        				+ ((1-zDiff) * getGridAltitude((int)xC,(int)zF)));
        	}
        	
        }else{
        	
        	if(zDiff + xDiff < 1.0f){
        		
        		//System.out.print("1: ");
        		
//        		double Q1_Y = ((zDiff) / (zC-zF) * getGridAltitude((int)xF,(int)zC))
//        				+ ((zC-z) / (zC-zF) * getGridAltitude((int)xC,(int)zF));

        		double Q1_Y = ((zDiff) * getGridAltitude((int)xF,(int)zC))
        				+ ((1-zDiff) * getGridAltitude((int)xC,(int)zF));

        		
        		double Q2_Y = (zDiff * getGridAltitude((int)xF,(int)zC)) + ((1-zDiff) * getGridAltitude((int)xF,(int)zF));
        		
        		double Y = ((x - xF) / ((xF+1-zDiff)-xF) * Q1_Y) + (((xF+1-zDiff)-x) / ((xF+1-zDiff)-xF) * Q2_Y);
        		
        		altitude = (float)Y;
        		
        	}else{
        		
        		//System.out.print("2: ");
        		
//        		double Q1_Y = ((z-zF) / (zC-zF) * getGridAltitude((int)xF,(int)zC))
//        				+ ((zC-z) / (zC-zF) * getGridAltitude((int)xC,(int)zF));
        		
        		double Q1_Y = ((zDiff) * getGridAltitude((int)xF,(int)zC))
        				+ ((1-zDiff) * getGridAltitude((int)xC,(int)zF));
        		
        		double Q3_Y = (zDiff * getGridAltitude((int)xC,(int)zC)) + ((1-zDiff) * getGridAltitude((int)xC,(int)zF));
        		
        		double Y1 = (x - (xF+1-zDiff)) / (xC-(xF+1-zDiff)) * Q3_Y;
        		double Y2 = (xC-x) / (xC-(xF+1-zDiff)) * Q1_Y;
        		
        		double Y = Y1 + Y2;
        		
        		altitude = (float)Y;
        		
        	}
        	
        }
        
        //System.out.println("X = " + x + ", Z =  "+ z + ", Alt = " + altitude);
        
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

    public void addPond(String Directory, String ImgType, float x, float y, float z, float scaleX, float scaleY){
    	
    	ponds.add(new Pond(Directory, ImgType, x, y, z, scaleX, scaleY));
    	
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
