package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.opengl.GL3;

import unsw.graphics.Application3D;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix4;
import unsw.graphics.Shader;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;



/**
 * COMMENT: Comment Game 
 *
 * @author malcolmr
 */
public class World extends Application3D {

    private Terrain terrain;
    private Camera cam;
    
    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        cam = new Camera();
    }
   
    /**
     * Load a level file and display it.
     * 
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        World world = new World(terrain);
        world.start();
    }

	@Override
	public void display(GL3 gl) {
		super.display(gl);
		
		cam.setHeight(terrain.altitude(cam.getPosition().getX(), cam.getPosition().getZ())+0.5f);
		
		cam.setView(gl);
		
		CoordFrame3D frame = CoordFrame3D.identity();
        
		terrain.draw(gl, frame);
		
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		
		terrain.Init(gl);
		getWindow().addKeyListener(cam);
		
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong_directional.glsl");
        shader.use(gl);

        // Set the lighting properties
        //Shader.setPoint3D(gl, "lightPos", terrain.getSunlight().asPoint3D());
        Shader.setPoint3D(gl, "lightDirection", terrain.getSunlight().asPoint3D());
        //Shader.setPoint3D(gl, "lightDirection", new Point3D(1, 0, 0)); /////TEST
        Shader.setColor(gl, "lightIntensity", new Color(0.9f, 0.9f, 0.9f));
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));
        
        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.6f, 0.6f, 0.6f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
        Shader.setColor(gl, "specularCoeff", new Color(0.5f, 0.5f, 0.5f));
        Shader.setFloat(gl, "phongExp", 10f);
		
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 0.1f, 100));
	}
}
