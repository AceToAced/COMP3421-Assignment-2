package unsw.graphics.world;

import java.util.List;

import unsw.graphics.Vector3;
import unsw.graphics.geometry.Point2D;

/**
 * COMMENT: Comment Road 
 *
 * @author malcolmr
 */
public class Road {

    private List<Point2D> points;
    private float width;
    
    /**
     * Create a new road with the specified spine 
     *
     * @param width
     * @param spine
     */
    public Road(float width, List<Point2D> spine) {
        this.width = width;
        this.points = spine;
    }

    /**
     * The width of the road.
     * 
     * @return
     */
    public double width() {
        return width;
    }
    
    /**
     * Get the number of segments in the curve
     * 
     * @return
     */
    public int size() {
        return points.size() / 3;
    }

    /**
     * Get the specified control point.
     * 
     * @param i
     * @return
     */
    public Point2D controlPoint(int i) {
        return points.get(i);
    }
    
    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * 
     * @param t
     * @return
     */
    public Point2D point(float t) {
        int i = (int)Math.floor(t);
        t = t - i;
        
        i *= 3;
        
        Point2D p0 = points.get(i++);
        Point2D p1 = points.get(i++);
        Point2D p2 = points.get(i++);
        Point2D p3 = points.get(i++);
        

        float x = b(0, t) * p0.getX() + b(1, t) * p1.getX() + b(2, t) * p2.getX() + b(3, t) * p3.getX();
        float y = b(0, t) * p0.getY() + b(1, t) * p1.getY() + b(2, t) * p2.getY() + b(3, t) * p3.getY();        
        
        return new Point2D(x, y);
    }
    
    /**
     * Calculate the Bezier coefficients
     * 
     * @param i
     * @param t
     * @return
     */
    private float b(int i, float t) {
        
        switch(i) {
        
        case 0:
            return (1-t) * (1-t) * (1-t);

        case 1:
            return 3 * (1-t) * (1-t) * t;
            
        case 2:
            return 3 * (1-t) * t * t;

        case 3:
            return t * t * t;
        }
        
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

    private float derivativeCoeff(int i, float t) {
        switch(i) {
            case 0:
                return (1-t) * (1-t);

            case 1:
                return 2*t*(1-t);
            case 2:
                return t*t;
        }
        // this should never happen
        throw new IllegalArgumentException("" + i);
    }

    public Point2D pointDerivative(float t) {
//        int i = (int)Math.floor(t);
//        t = t - i;

//        i *= 3;
        
        
//        Point2D p0 = points.get(i++);
//        Point2D p1 = points.get(i++);
//        Point2D p2 = points.get(i++);
//        Point2D p3 = points.get(i++);

        Vector3 p0 = points.get(0).asHomogenous();
        Vector3 p1 = points.get(1).asHomogenous();
        Vector3 p2 = points.get(2).asHomogenous();
        Vector3 p3 = points.get(3).asHomogenous();

        Vector3 p01 = p1.plus(p0.negate());
        p01 = p01.scale(derivativeCoeff(0, t));
        Vector3 p12 = p2.plus(p1.negate());
        p12 = p12.scale(derivativeCoeff(1, t));
        Vector3 p23 = p3.plus(p2.negate());
        p23 = p23.scale(derivativeCoeff(2, t));

        return (p01.plus(p12).plus(p23)).scale(3).asPoint2D();
    }


}
