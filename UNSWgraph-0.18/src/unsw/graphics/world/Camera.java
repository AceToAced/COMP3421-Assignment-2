package unsw.graphics.world;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.GL3;
import unsw.graphics.CoordFrame3D;
import unsw.graphics.Matrix3;
import unsw.graphics.Shader;
import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;
import unsw.graphics.geometry.Point3D;
import unsw.graphics.scene.MathUtil;

/**
 * The camera is a SceneObject that can be moved, rotated and scaled like any other, as well as
 * attached to any parent in the scene tree.
 * 
 * TODO: You need to implement the setView() method.
 *
 * @author malcolmr
 * @author Robert Clifton-Everest
 */
public class Camera implements KeyListener{
    
    /**
     * The aspect ratio is the ratio of the width of the window to the height.
     */
    private float myAspectRatio;
    private float scale;
    private Point3D Position;
    
    private float RotationX;
    private float RotationY;
    private float RotationZ;
    private float MovementSpeed = 0.5f;
    
    public Camera() {
    	
    	myAspectRatio = 1.0f;
    	scale = 1;
    	Position = new Point3D(0, 0, 0);
    
    	RotationX = 0;
        RotationY = 0;
        RotationZ = 0;
    	
    }

    public void setView(GL3 gl) {

    	
    	CoordFrame3D Aspect = CoordFrame3D.identity().scale(1/myAspectRatio, 1,1);
    	
    	CoordFrame3D viewFrame = CoordFrame3D.identity()
                .scale(1/scale, 1/scale, 1/scale)
                .rotateX(-RotationX)
                .rotateY(-RotationY)
    			.rotateZ(-RotationZ)
                .translate(Position.getX(), Position.getY(),Position.getZ());
    	
    	viewFrame = new CoordFrame3D(Aspect.getMatrix().multiply(viewFrame.getMatrix()));
    	
        Shader.setViewMatrix(gl, viewFrame.getMatrix());

    }

    public void reshape(int width, int height) {
        myAspectRatio = (1f * width) / height;            
    }

    /**
     * Transforms a point from camera coordinates to world coordinates. Useful for things like mouse
     * interaction
     * 
     * @param x
     * @param y
     * @return
     */
//    public Point2D fromView(float x, float y) {
//        Matrix3 mat = Matrix3.translation(getGlobalPosition())
//                .multiply(Matrix3.rotation(getGlobalRotation()))
//                .multiply(Matrix3.scale(getGlobalScale(), getGlobalScale()))
//                .multiply(Matrix3.scale(myAspectRatio, 1));
//        return mat.multiply(new Vector3(x,y,1)).asPoint2D();
//    }

    public float getAspectRatio() {
        return myAspectRatio;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
    	
    	double radians;
    	
    	
        switch(e.getKeyCode()) {
        case KeyEvent.VK_LEFT:
        	
        	RotationY = MathUtil.normaliseAngle(RotationY+2);
        	//System.out.println(RotationY);
        	break;
        case KeyEvent.VK_RIGHT:
        	
        	
        	RotationY = MathUtil.normaliseAngle(RotationY-2);
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
        }

        
        //System.out.println(RotationY);
    	//System.out.println(Math.toRadians(RotationY));
    	//System.out.println("SIN = " + (float)(-Math.sin(Math.toRadians(RotationY))));
    	//System.out.println("COS = " + (float)(Math.cos(Math.toRadians(RotationY))));
        
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    
}

