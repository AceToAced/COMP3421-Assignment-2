package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;

public class Pond {

	private float scale;
    private Point3D Position;
    
    private TriangleMesh base;
	private ArrayList<Texture> textures;
    private int TexFrames = 20;
    private int frameNum = 0;
    private File Directory;
    private String ImgType;
    
	public Pond(String directory, String ImgType){
		
		Directory = new File(directory);
		this.ImgType = ImgType;
		
		scale = 1;	
		Position = new Point3D(0,0,0);
		textures = new ArrayList<Texture>();
	}
	
	public Pond(String directory, String ImgType, float x ,float y, float z){
		
		Directory = new File(directory);
		this.ImgType = ImgType;
		
		textures = new ArrayList<Texture>();
		
		scale = 1;
		Position = new Point3D(x,y,z);
	}
	
	public Pond(String directory, String ImgType, float x ,float y, float z, float scale){
		
		Directory = new File(directory);
		this.ImgType = ImgType;
		
		this.scale = scale;
		Position = new Point3D(x,y,z);
		textures = new ArrayList<Texture>();
	}
	
	public void Init(GL3 gl){
		
		List<Point3D> vertexList = new ArrayList<Point3D>();
		List<Point2D> texList = new ArrayList<Point2D>();
    	List<Integer> indicesList = new ArrayList<Integer>();
    	
    	vertexList.add(new Point3D(-0.5f,0,0.5f)); //top left : 0
    	vertexList.add(new Point3D(0.5f,0,0.5f));  //top right : 1
    	vertexList.add(new Point3D(0.5f,0,-0.5f)); //bottom right : 2
    	vertexList.add(new Point3D(-0.5f,0,-0.5f));	//bottom left : 3
    	
    	texList.add(new Point2D(0,0));
    	texList.add(new Point2D(1,0));
    	texList.add(new Point2D(1,1));
    	texList.add(new Point2D(0,1));
		
    	indicesList.add(0);
    	indicesList.add(2);
    	indicesList.add(3);
    	
    	indicesList.add(0);
    	indicesList.add(1);
    	indicesList.add(2);
    	
    	base = new TriangleMesh(vertexList, indicesList,true, texList);
    	base.init(gl);
    	
    	ArrayList<String> temp = new ArrayList<>(Arrays.asList(Directory.list()));
    	
    	for(int i = 0; i < temp.size(); i++){

    		textures.add(new Texture(gl, "res/textures/Pond/" + temp.get(i) , ImgType, false));
    	}
    	
	}
	
	public void draw(GL3 gl, CoordFrame3D frame){
		
		Shader.setInt(gl, "tex", 0);
        
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textures.get(frameNum).getId());
    	
    	Shader.setPenColor(gl, Color.WHITE);	
    	
    	base.draw(gl, frame.translate(Position).scale(scale,scale, scale));
    	
    	frameNum++;
		frameNum = frameNum % textures.size();
	}
	
}
