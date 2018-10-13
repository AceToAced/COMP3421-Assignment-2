package unsw.graphics.world;

import java.awt.Color;
import java.io.IOException;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;

import unsw.graphics.CoordFrame3D;
import unsw.graphics.Shader;
import unsw.graphics.Texture;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.geometry.TriangleMesh;
import unsw.graphics.scene.MathUtil;

public class Avatar implements KeyListener {

	
	private float myAspectRatio;
    private float scale;
    private Point3D Position;
    private Camera cam;
    private TriangleMesh avatar;
    private Texture texture;
    private boolean firstPerson = false;
    
    private float RotationX;
    private float RotationY;
    private float RotationZ;
    private float MovementSpeed = 0.5f;
	
    public Avatar() {
    	
    	cam = new Camera(0,0.3f,-0.7f);
    	
    	myAspectRatio = 1.0f;
    	scale = 1;
    	Position = new Point3D(0, 0, 0);
    
    	RotationX = 0;
        RotationY = 0;
        RotationZ = 0;
    	
    }
    
    public Avatar(float x, float y, float z) {
    	
    	cam = new Camera(0,0.3f,-0.7f);
    	
    	myAspectRatio = 1.0f;
    	scale = 1;
    	Position = new Point3D(x, y, z);
    	
    	RotationX = 0;
    	RotationY = 0;
    	RotationZ = 0;
    	
    }
    
    public void Init(GL3 gl){
    	
    	try {
			avatar = new TriangleMesh("res/models/bunny.ply",true,true);
			avatar.init(gl);
		} catch (IOException e) {
			System.out.println("Avatar file not found/couldnt load file");
			e.printStackTrace();
			System.exit(1);
			
		}
    	
    	texture = new Texture(gl, "res/textures/BrightPurpleMarble.png", "png", false);
    	
    }
    
    public void draw(GL3 gl,CoordFrame3D frame){
    	
    	if(!firstPerson){
    	
	    	Shader.setInt(gl, "tex", 0);
	        
	        gl.glActiveTexture(GL.GL_TEXTURE0);
	        gl.glBindTexture(GL.GL_TEXTURE_2D, texture.getId());
	    	
	    	Shader.setPenColor(gl, Color.WHITE);
	    	
	    	//System.out.println("x: " + Position.getX() + ", y:" + Position.getY() + ", z:" + Position.getZ());
	    	
	    	avatar.draw(gl,frame.translate(Position)
	    			.rotateX(RotationX)
	    			.rotateY(MathUtil.normaliseAngle(RotationY+90))
	    			.rotateZ(RotationZ)
	    			.scale(scale, scale, scale));
    	}
    }
    
    public void setHeight(float Y){
    	Position = new Point3D( Position.getX(), Y, Position.getZ());
    }
    
    public Point3D getPosition(){
    	return Position;
    }
    
    public void setView(GL3 gl, CoordFrame3D frame){
    	
    	cam.setView(gl, frame.translate(Position)
    			.rotateX(RotationX)
    			.rotateY(RotationY)
    			.rotateZ(RotationZ)
    			.scale(scale, scale, scale), this);
    	
    	
//    	cam.setView(gl, frame.scale(1/scale, 1/scale, 1/scale)
//    			.rotateX(-RotationX)
//    			.rotateY(-RotationY)
//    			.rotateZ(-RotationZ)
//    			.translate(-Position.getX(), -Position.getY(),-Position.getZ())
//    			);
    	
    }
    
    public float getRotx(){
    	return RotationX;
    }
    
    public float getRoty(){
    	return RotationY;
    }
    
    public float getRotz(){
    	return RotationZ;
    }
    
    public float getScale(){
    	return scale;
    }
    
	@Override
    public void keyPressed(KeyEvent e) {
    	
    	double radians;
    	
    	
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        	
        	RotationY = MathUtil.normaliseAngle(RotationY+3);
        	//System.out.println(RotationY);
        	break;
        case KeyEvent.VK_RIGHT:
        	
        	
        	RotationY = MathUtil.normaliseAngle(RotationY-3);
        	//System.out.println(RotationY);
        	break;
        case KeyEvent.VK_DOWN:
        	
        	radians = Math.toRadians(RotationY);
        	
        	//System.out.println("UP - " + Position.getZ());
        	Position = new Point3D(Position.getX()-(MovementSpeed*(float)(Math.sin(radians)))
        			,Position.getY()
        			,Position.getZ()-(MovementSpeed*(float)(Math.cos(radians))));
        	break;
        case KeyEvent.VK_UP:
            
        	radians = Math.toRadians(RotationY);
        	
        	//System.out.println("UP - " + Position.getZ());
        	Position = new Point3D(Position.getX()+(MovementSpeed*(float)(Math.sin(radians)))
        			,Position.getY()
        			,Position.getZ()+(MovementSpeed*(float)(Math.cos(radians))));
            break;
        	
        case KeyEvent.VK_F:
            
        	if(firstPerson){
        		
        		firstPerson = false;
        		cam.setPosition(new Point3D(0,0.3f,-0.7f));
        		
        	}else{
        		
        		firstPerson = true;
        		cam.setPosition(new Point3D(0,0.2f,0));
        	}
        
        	break;
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {}
	
}
