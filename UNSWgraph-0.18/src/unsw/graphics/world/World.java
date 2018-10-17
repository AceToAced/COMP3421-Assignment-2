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
    private Avatar avatar;
    private Pond pond;
    
    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        
        avatar = new Avatar();
        pond = new Pond("res/textures/Pond", "jpg", -1, 0, -1);
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
		
		avatar.setHeight(terrain.altitude(avatar.getPosition().getX(), avatar.getPosition().getZ()));//+0.5f);
		
		CoordFrame3D frame = CoordFrame3D.identity();
		
		avatar.setView(gl, frame);
		avatar.draw(gl, frame);
        
		terrain.draw(gl, frame);
		
		pond.draw(gl, frame);
		
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		
		avatar.Init(gl);
		terrain.Init(gl);
		pond.Init(gl);
		
		getWindow().addKeyListener(avatar);
		
		Shader shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong_directional.glsl");
        shader.use(gl);

        // Set the lighting properties
        Shader.setPoint3D(gl, "lightDirection", terrain.getSunlight().asPoint3D());
        Shader.setColor(gl, "lightIntensity", new Color(0.9f, 0.9f, 0.9f));
        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));
        
        // Set the material properties
        Shader.setColor(gl, "ambientCoeff", new Color(0.6f, 0.6f, 0.6f));
        Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
        Shader.setColor(gl, "specularCoeff", new Color(0.1f, 0.1f, 0.1f));
        Shader.setFloat(gl, "phongExp", 32f);
		
	}

	@Override
	public void reshape(GL3 gl, int width, int height) {
        super.reshape(gl, width, height);
        Shader.setProjMatrix(gl, Matrix4.perspective(60, width/(float)height, 0.1f, 100));
	}
}
