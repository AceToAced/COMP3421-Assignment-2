package unsw.graphics.world;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
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
    private boolean torch;
    private boolean torchPrev;
    private Shader shader;
    
    public World(Terrain terrain) {
    	super("Assignment 2", 800, 600);
        this.terrain = terrain;
        
        avatar = new Avatar();
        torch = false;
        torchPrev = true;
        //pond = new Pond("res/textures/Pond", "jpg", 1.5f, 1, 3.5f);
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
		
		if(torch){
			
			Point3D temp = avatar.getPosition();
			Shader.setPoint3D(gl, "lightPos", new Point3D(temp.getX(), temp.getY()+0.3f, temp.getZ()));
			
		}
		
		CoordFrame3D frame = CoordFrame3D.identity();
		
		avatar.setView(gl, frame);
		avatar.draw(gl, frame);
        
		terrain.draw(gl, frame);
		
		if(torch == torchPrev){
			
			torchPrev = !torch;
			
			if(torch){
				
				shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
		                "shaders/fragment_tex_phong_torch.glsl");
		        shader.use(gl);

		        Shader.setPoint3D(gl, "lightPos", avatar.getPosition());
		        Shader.setPoint3D(gl, "torchDirection", new Point3D(0,0,1));
		        
		        Shader.setColor(gl, "lightIntensity", Color.WHITE);
		        Shader.setColor(gl, "ambientIntensity", new Color(0.1f, 0.1f, 0.1f));
		        
		        Shader.setColor(gl, "ambientCoeff", new Color(0.6f, 0.6f, 0.6f));
		        Shader.setColor(gl, "diffuseCoeff", Color.WHITE);
		        Shader.setColor(gl, "specularCoeff", new Color(0.1f, 0.1f, 0.1f));
		        Shader.setFloat(gl, "phongExp", 32f);
		        Shader.setFloat(gl, "cutoff", (float)Math.cos(26f*(Math.PI/180)));
		        Shader.setFloat(gl, "cutoffDistance", 4.0f);
				
			}else{
				
				shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
		                "shaders/fragment_tex_phong_directional.glsl");
		        shader.use(gl);

		        Shader.setPoint3D(gl, "lightDirection", terrain.getSunlight().asPoint3D());
		        Shader.setColor(gl, "lightIntensity", new Color(0.9f, 0.9f, 0.9f));
		        Shader.setColor(gl, "ambientIntensity", new Color(0.2f, 0.2f, 0.2f));
		        
		        Shader.setColor(gl, "ambientCoeff", new Color(0.6f, 0.6f, 0.6f));
		        Shader.setColor(gl, "diffuseCoeff", new Color(0.6f, 0.6f, 0.6f));
		        Shader.setColor(gl, "specularCoeff", new Color(0.1f, 0.1f, 0.1f));
		        Shader.setFloat(gl, "phongExp", 32f);
				
			}
			
		}
		
		
	}

	@Override
	public void destroy(GL3 gl) {
		super.destroy(gl);
		
	}

	@Override
	public void init(GL3 gl) {
		super.init(gl);
		
		System.out.println((float)Math.cos(15f*(Math.PI/180)));
		
		avatar.Init(gl);
		terrain.Init(gl);
		
		getWindow().addKeyListener(avatar);
		getWindow().addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {}
			
			@Override
			public void keyPressed(KeyEvent arg0) {

				if(arg0.getKeyCode() == KeyEvent.VK_SPACE){
					torch = !torch;
				}
				
			}
		});
		
		shader = new Shader(gl, "shaders/vertex_tex_phong.glsl",
                "shaders/fragment_tex_phong_directional.glsl");
        shader.use(gl);

        // Set the lighting properties
        Shader.setPoint3D(gl, "lightDirection", terrain.getSunlight().asPoint3D());
        //Shader.setPoint3D(gl, "lightPos", avatar.getPosition());
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
