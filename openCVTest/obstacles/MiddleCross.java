package obstacles;

import moveableObjects.Coordinate;

public class MiddleCross {

	private Coordinate centerOfCross, topCross, bottomCross, leftCross,
			rightCross;

	public MiddleCross(double x, double y) {
		this.centerOfCross = new Coordinate(x, y);
	}

	public Coordinate getCenterOfCross() {
		return centerOfCross;
	}

	public void setCenterOfCross(Coordinate centerOfCross) {
		this.centerOfCross = centerOfCross;
	}

	public Coordinate getTopCross() {
		return topCross;
	}

	public void setTopCross(Coordinate topCross) {
		this.topCross = topCross;
	}

	public Coordinate getBottomCross() {
		return bottomCross;
	}

	public void setBottomCross(Coordinate bottomCross) {
		this.bottomCross = bottomCross;
	}

	public Coordinate getLeftCross() {
		return leftCross;
	}

	public void setLeftCross(Coordinate leftCross) {
		this.leftCross = leftCross;
	}

	public Coordinate getRightCross() {
		return rightCross;
	}

	public void setRightCross(Coordinate rightCross) {
		this.rightCross = rightCross;
	}

}
