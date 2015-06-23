package imageProcess;

import imageCapture.ImageObject;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import javafx.scene.shape.Circle;
import moveableObjects.Ball;
import moveableObjects.Coordinate;
import moveableObjects.Robot;
import obstacles.Goal;
import obstacles.MiddleCross;
import obstacles.ObstacleFrame;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import pathfinding.Pathfinder;

public class ImageProcessing {
	// constant for camera
	private final double heightOfCamera = 217;
	private boolean foundWallsBefore = false;
	private int imgCaptures = 1;
	private Coordinate topLeft, topRight, lowLeft, lowRight;
	private Pathfinder pathfinder = new Pathfinder();
	public int ballSize = 7;

	public int iLowH = 16;
	public int iLowHFront = 33;

	public int iHighH = 255;
	public int iHighHFront = 108;

	public int iLowS = 96;
	public int iLowSFront = 71;

	public int iHighS = 255;
	public int iHighSFront = 255;

	public int iLowV = 219;
	public int iLowVFront = 77;

	public int iHighV = 255;
	public int iHighVFront = 184;

	private ObstacleFrame frames = new ObstacleFrame();
	private Robot robot = new Robot();
	private ArrayList<Ball> balls;
	private MiddleCross cross;
	private Goal goalA, goalB;

	private ArrayList<Point> lineCoordinates;
	private Mat image, frame, imgHSV, imageBlurr;
	private Image backgroundImage;
	private Rect fieldRect, crossRect, goalRect;

	public void process() {

		// Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		frame = new Mat();
		image = new Mat();

		balls = new ArrayList<Ball>();
		lineCoordinates = new ArrayList<Point>();

		frame = ImageObject.getInstance().getImg();
		// if the frame was empty we should return
		if (frame == null) {
			return;
		}
		Highgui.imwrite("testImage.jpg", frame);
		// Consider the image for processing Imgproc.COLOR_BGR2GRAY

		try {
			frame.copyTo(image);
		} catch (Exception e) {
			System.out.println("error happened in copy");
		}
		imgHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
		imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
		try {
			Imgproc.cvtColor(frame, imgHSV, Imgproc.COLOR_BGR2GRAY);
			Imgproc.GaussianBlur(imgHSV, imageBlurr, new Size(3, 3), 0, 0);
		} catch (Exception e) {
			System.out.println("frame was empty returning");
			return;
		}
		// this is bad practice
		try {
			findRobotFrontAndBack();

			identifyLines();
			findBallsInImage();
			projectAllCoordinates();

			backgroundImage = toBufferedImage(image);
		} catch (Exception e) {
			System.out.println("error occured stop the robot!");
			pathfinder.stopAllCommands();
			return;
		}

		try {
			pathfinder
					.findPath(robot, balls, goalA, goalB, null, frames, cross);
		} catch (Exception e) {
			System.out.println("Error happened in pathfinding");
			return;
		}
	}

	private void projectAllCoordinates() {
		// constants that were measured
		double heightOfRobot = 20;
		double heightOfBall = 4;
		double heightOfGoal = 7;

		projectCoordinate(robot.getFrontCord(), heightOfRobot);
		projectCoordinate(robot.getBackCord(), heightOfRobot);
		robot.updateMiddleCord();

		for (Ball ball : balls) {
			projectCoordinate(ball, heightOfBall);
		}

		projectCoordinate(goalA, heightOfGoal);
		projectCoordinate(goalB, heightOfGoal);

		projectCoordinate(frames.topLeft(), heightOfGoal);
		projectCoordinate(frames.topRight(), heightOfGoal);
		projectCoordinate(frames.lowLeft(), heightOfGoal);
		projectCoordinate(frames.lowRight(), heightOfGoal);

	}

	private void findRobotFrontAndBack() {
		// find the robot with color scan
		Mat imgOriginal = new Mat();
		frame.copyTo(imgOriginal);
		Mat[] robotMats = new Mat[2];
		Mat imageHSV = new Mat();
		Imgproc.cvtColor(imgOriginal, imageHSV, Imgproc.COLOR_BGR2HSV);

		// we have two parts of the robot we want to find
		Mat imgThresholded = new Mat();
		Mat imgThresholded2 = new Mat();
		robotMats[0] = imgThresholded;
		robotMats[1] = imgThresholded2;

		// back of the robot
		Core.inRange(imageHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(
				iHighH, iHighS, iHighV), imgThresholded);

		// front of the robot
		Core.inRange(imageHSV, new Scalar(iLowHFront, iLowSFront, iLowVFront),
				new Scalar(iHighHFront, iHighSFront, iHighVFront),
				imgThresholded2);

		// check for both the front and back of the robot.
		for (int j = 0; j < robotMats.length; j++) {

			// morphological opening (removes small objects from the
			// foreground)
			Imgproc.erode(robotMats[j], robotMats[j], Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));
			Imgproc.dilate(robotMats[j], robotMats[j], Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));

			// morphological closing (removes small holes from the
			// foreground)
			Imgproc.erode(robotMats[j], robotMats[j], Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));
			Imgproc.dilate(robotMats[j], robotMats[j], Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));

			Moments oMoments = Imgproc.moments(robotMats[j], true);

			double dM01 = oMoments.get_m01();
			double dM10 = oMoments.get_m10();
			double dArea = oMoments.get_m00();

			// if the area <= 10000, I consider that the there are no object
			// in
			// the image and it's because of the noise, the area is not zero
			if (dArea > 50) {
				// calculate the position of the ball
				double posX = dM10 / dArea;
				double posY = dM01 / dArea;

				// add the robot objects to the ArrayList for pathfinding
				if (j == 0) {
					robot.setBackCord(new Coordinate(posX, posY));
					robot.setAreaBack(new Circle(posX, posY, 3.5 * Math
							.sqrt(dArea / 3.14)));
					Core.circle(image, new Point(posX, posY), (int) (3.5 * Math
							.sqrt(dArea / 3.14)), new Scalar(255, 255, 255));
					robot.robotRadius = (int) Math.sqrt(dArea / 3.14);
				} else {
					robot.setFrontCord(new Coordinate(posX, posY));
					robot.setAreaFront(new Circle(posX, posY, 3.5 * Math
							.sqrt(dArea / 3.14)));
					Core.circle(image, new Point(posX, posY), (int) (3.5 * Math
							.sqrt(dArea / 3.14)), new Scalar(255, 255, 255));

				}
			}

		}

		robotMats[0].copyTo(robotMats[1], robotMats[0]);
		// convert the mats to one mat and convert it to an Image
		robot.updateMiddleCord();

	}

	private void findBallsInImage() {
		/*
		 * Find the circles in the image
		 */
		Mat circles = new Mat();
		Imgproc.HoughCircles(imageBlurr, circles, Imgproc.CV_HOUGH_GRADIENT, 1,
				10, 90, 15, 1, 15);

		if (!circles.empty()) {

			for (int i = 0; i <= circles.cols(); i++) {

				double[] coordinate = circles.get(0, i);
				if (coordinate == null) {
					break;
				}
				/*
				 * only let the ball be visible if is not within any of the
				 * prohibited areas of the field
				 */
				if (!robot.getRobotFrontArea().contains(coordinate[0],
						coordinate[1])
						&& !robot.getRobotBackArea().contains(coordinate[0],
								coordinate[1])
						&& fieldRect.contains(new Point(coordinate[0],
								coordinate[1]))
						&& !crossRect.contains(new Point(coordinate[0],
								coordinate[1]))
						&& !goalRect.contains(new Point(coordinate[0],
								coordinate[1]))) {
					balls.add(new Ball(coordinate[0], coordinate[1]));
					balls.get(balls.size() - 1).setRadius(coordinate[2]);
				}
			}
		}
	}

	private void identifyLines() throws Exception {
		/*
		 * Find the lines representing the edge of the field
		 */

		Mat dst = new Mat();
		imgHSV.copyTo(dst);
		Imgproc.Canny(dst, dst, 50, 200, 3, false);

		Mat lines = new Mat();

		Imgproc.HoughLinesP(dst, lines, 1, Math.PI / 180, 100, 200, 600);

		for (int x = 0; x < lines.cols(); x++) {
			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			Point start = new Point(x1, y1);
			Point end = new Point(x2, y2);
			lineCoordinates.add(start);
			lineCoordinates.add(end);

		}
		// approximate how the field acctually looks.
		drawApproxLines();

	}

	private void drawApproxLines() {

		Rect topLeftRect = new Rect(new Point(0, 0), new Point(200, 200));
		Rect topRightRect = new Rect(new Point(440, 0), new Point(640, 240));
		Rect bottomLeftRect = new Rect(new Point(0, 300), new Point(200, 480));
		Rect bottomRightRect = new Rect(new Point(440, 300),
				new Point(640, 480));

		Point lineTopLeft;
		Point lineTopRight;
		Point lineBottomLeft;
		Point lineBottomRight;
		int count = 0;
		int x = 0;
		int y = 0;
		// find all the points closest to top left corner
		for (int i = 0; i < lineCoordinates.size(); i++) {
			if (lineCoordinates.get(i).inside(topLeftRect)) {
				count++;
				x += lineCoordinates.get(i).x;
				y += lineCoordinates.get(i).y;
			}
		}
		x /= count;
		y /= count;
		lineTopLeft = new Point(x, y);

		x = 0;
		y = 0;
		count = 0;

		// find all the points closest to top right corner
		for (int i = 0; i < lineCoordinates.size(); i++) {
			if (lineCoordinates.get(i).inside(topRightRect)) {
				count++;
				x += lineCoordinates.get(i).x;
				y += lineCoordinates.get(i).y;
			}
		}
		x /= count;
		y /= count;

		lineTopRight = new Point(x, y);

		x = 0;
		y = 0;
		count = 0;

		// find all the points closest to bottom left corner
		for (int i = 0; i < lineCoordinates.size(); i++) {
			if (lineCoordinates.get(i).inside(bottomLeftRect)) {
				count++;
				x += lineCoordinates.get(i).x;
				y += lineCoordinates.get(i).y;
			}
		}
		x /= count;
		y /= count;
		lineBottomLeft = new Point(x, y);
		x = 0;
		y = 0;
		count = 0;

		// find all the points closest to bottom right corner
		for (int i = 0; i < lineCoordinates.size(); i++) {
			if (lineCoordinates.get(i).inside(bottomRightRect)) {
				count++;
				x += lineCoordinates.get(i).x;
				y += lineCoordinates.get(i).y;
			}
		}
		x /= count;
		y /= count;
		lineBottomRight = new Point(x, y);

		// add the corners to the list of objects that we have to take into
		// consideration
		if (!foundWallsBefore) {
			lowLeft = new Coordinate(lineBottomLeft.x, lineBottomLeft.y);
			lowRight = new Coordinate(lineBottomRight.x, lineBottomRight.y);
			topLeft = new Coordinate(lineTopLeft.x, lineTopLeft.y);
			topRight = new Coordinate(lineTopRight.x, lineTopRight.y);

			frames.setTopLeft(new Coordinate(lineTopLeft.x, lineTopLeft.y));
			frames.setTopRight(new Coordinate(lineTopRight.x, lineTopRight.y));
			frames.setLowRight(new Coordinate(lineBottomRight.x,
					lineBottomRight.y));
			frames.setLowLeft(new Coordinate(lineBottomLeft.x, lineBottomLeft.y));
			foundWallsBefore = true;
		} else {
			lowLeft.setX(lowLeft.getX() + lineBottomLeft.x);
			lowLeft.setY(lowLeft.getY() + lineBottomLeft.y);
			lowRight.setX(lowRight.getX() + lineBottomRight.x);
			lowRight.setY(lowRight.getY() + lineBottomRight.y);
			topLeft.setX(topLeft.getX() + lineTopLeft.x);
			topLeft.setY(topLeft.getY() + lineTopLeft.y);
			topRight.setX(topRight.getX() + lineTopRight.x);
			topRight.setY(topRight.getY() + lineTopRight.y);

			frames.setTopLeft(new Coordinate(topLeft.getX() / imgCaptures,
					topLeft.getY() / imgCaptures));
			frames.setTopRight(new Coordinate(topRight.getX() / imgCaptures,
					topRight.getY() / imgCaptures));
			frames.setLowRight(new Coordinate(lowRight.getX() / imgCaptures,
					lowRight.getY() / imgCaptures));
			frames.setLowLeft(new Coordinate(lowLeft.getX() / imgCaptures,
					lowLeft.getY() / imgCaptures));
		}
		// add the rectangle covering the field, to avoid getting unwanted balls
		fieldRect = new Rect(new Point(frames.topLeft().getX() + ballSize,
				frames.topLeft().getY() + ballSize), new Point(frames
				.lowRight().getX() - ballSize, frames.lowRight().getY()
				- ballSize));

		// calculate the cross middle point
		x = (int) ((frames.topLeft().getX() + frames.topRight().getX()) / 2);
		y = (int) ((frames.topLeft().getY() + frames.lowLeft().getY()) / 2);

		// Add the cross object and set all the points
		cross = new MiddleCross(x, y);

		cross.setLeftCross(new Coordinate(cross.getCenterOfCross().getX()
				- ((frames.topRight().getX() - frames.topLeft().getX()) / 18)
				- robot.robotRadius, cross.getCenterOfCross().getY()));

		cross.setRightCross(new Coordinate(cross.getCenterOfCross().getX()
				+ ((frames.topRight().getX() - frames.topLeft().getX()) / 18)
				+ robot.robotRadius, cross.getCenterOfCross().getY()));

		cross.setTopCross(new Coordinate(cross.getCenterOfCross().getX(), cross
				.getCenterOfCross().getY()
				- ((frames.lowRight().getY() - frames.topRight().getY()) / 12)
				- robot.robotRadius));

		cross.setBottomCross(new Coordinate(
				cross.getCenterOfCross().getX(),
				cross.getCenterOfCross().getY()
						+ ((frames.lowRight().getY() - frames.topRight().getY()) / 12)
						+ robot.robotRadius));

		// create a rectangle around the cross to ignore all the balls there.
		crossRect = new Rect(new Point(cross.getLeftCross().getX()
				- robot.robotRadius, cross.getTopCross().getY()
				- robot.robotRadius), new Point(cross.getRightCross().getX()
				- robot.robotRadius, cross.getBottomCross().getY()
				- robot.robotRadius));
		// add Goal points to the list of objects (where the robot )
		goalA = new Goal(
				((frames.topLeft().getX() + frames.lowLeft().getX()) / 2),
				((frames.topLeft().getY() + frames.lowLeft().getY()) / 2));
		goalB = new Goal(
				((frames.topRight().getX() + frames.lowRight().getX()) / 2),
				(frames.topRight().getY() + frames.lowRight().getY()) / 2);

		goalRect = new Rect(new Point(goalA.getX() - robot.robotRadius * 3,
				goalA.getY() - robot.robotRadius * 3),
				new Point(goalA.getX() + robot.robotRadius * 3, goalA.getY()
						+ (robot.robotRadius) * 3));

		imgCaptures++;
	}

	/**
	 * convert a Mat object to a buffered Image
	 * 
	 * @param m
	 * @return Image
	 */
	public Image toBufferedImage(Mat m) {
		int type = BufferedImage.TYPE_BYTE_GRAY;
		if (m.channels() > 1) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		int bufferSize = m.channels() * m.cols() * m.rows();
		byte[] b = new byte[bufferSize];
		m.get(0, 0, b); // get all the pixels
		BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
		final byte[] targetPixels = ((DataBufferByte) image.getRaster()
				.getDataBuffer()).getData();
		System.arraycopy(b, 0, targetPixels, 0, b.length);
		return image;

	}

	private void projectCoordinate(Coordinate cord, double heightOfTarget) {

		double centerX = frame.width() / 2;
		double centerY = frame.height() / 2;

		double newX = ((cord.getX() - centerX) * ((heightOfCamera - heightOfTarget) / heightOfCamera))
				+ centerX;
		double newY = ((cord.getY() - centerY) * ((heightOfCamera - heightOfTarget) / heightOfCamera))
				+ centerY;

		cord.setX(newX);
		cord.setY(newY);

	}

	// standard getters and setters used by other classes

	public Image getBackgroundImage() {
		return this.backgroundImage;
	}

	public ArrayList<Ball> getBalls() {
		return this.balls;
	}

	public Robot getRobot() {
		return this.robot;
	}

	public Goal getGoalA() {
		return this.goalA;
	}

	public Goal getGoalB() {
		return this.goalB;
	}

	public ObstacleFrame getFrames() {
		return this.frames;
	}

	public MiddleCross getCross() {
		return this.cross;
	}
}
