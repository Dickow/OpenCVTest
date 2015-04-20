package imageProcess;

import java.awt.Point;

public class Vector2D {
	private NodeObjects point1;
	private NodeObjects point2;

	public Vector2D(NodeObjects p1, NodeObjects p2) {
		this.point1 = p1;
		this.point2 = p2;
	}

	/**
	 * Determines the angle of a straight line drawn between point one and two.
	 * The number returned, which is a double in degrees, tells us how much we
	 * have to rotate a horizontal line clockwise for it to match the line
	 * between the two points. If you prefer to deal with angles using radians
	 * instead of degrees, just change the last line to:
	 * "return Math.atan2(yDiff, xDiff);"
	 */
	public static double GetAngleOfLineBetweenTwoPoints(Vector2D one) {
		
		double xDiff = one.point2.getX() - one.point1.getX();
		double yDiff = one.point2.getY() - one.point1.getY();
//		double xDiff = p2.x - p1.x;
//		double yDiff = p2.y - p1.y;
		
		return Math.toDegrees(Math.atan2(yDiff, xDiff));
	}
}
