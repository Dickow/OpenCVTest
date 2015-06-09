package imageProcess;

import imageCapture.ImageObject;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

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

public class ImageProcessing implements Runnable {
	private Pathfinder pathfinder = new Pathfinder();
	public int ballSize = 5;

	public int iLowH = 0;
	public int iLowH2 = 133;

	public int iHighH = 138;
	public int iHighH2 = 255;

	public int iLowS = 173;
	public int iLowS2 = 145;

	public int iHighS = 255;
	public int iHighS2 = 255;

	public int iLowV = 119;
	public int iLowV2 = 51;

	public int iHighV = 255;
	public int iHighV2 = 255;

	public Image outImg, outImg2;

	private ObstacleFrame frames = new ObstacleFrame();
	private Robot robot = new Robot();
	private ArrayList<Ball> balls;
	private MiddleCross cross;
	private Goal goalA, goalB;

	public ArrayList<Point> lineCoordinates;
	private Mat image;

	public void run() {

		// Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat frame = new Mat();

		while (true) {
			balls = new ArrayList<Ball>();
			lineCoordinates = new ArrayList<Point>();

			frame = ImageObject.getInstance().getImg();

			Highgui.imwrite("cameraInput.jpg", frame);

			// Consider the image for processing Imgproc.COLOR_BGR2GRAY
			image = Highgui.imread("cameraInput.jpg");
			Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
			Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
			try {
				Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
			} catch (Exception e) {
				System.out.println("frame was empty returning");
				continue;
			}
			Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(3, 3), 0, 0);

			identifyLines();

			findRobotFrontAndBack();

			findBallsInImage(imageBlurr);

			try {
				drawVectors();
				ignoreBallInsideRobot();
				drawBalls();
			} catch (Exception e) {
				// do nothing
			}
			outImg2 = toBufferedImage(image);

			// TODO call the pathfinder here
			try {
				pathfinder.findPath(robot, balls, goalA, goalB, frames, cross);
			} catch (Exception e) {
				System.out.println("Error happened");
			}
		}

	}

	private void findRobotFrontAndBack() {
		// find the robot with color scan

		Mat imgOriginal = Highgui.imread("cameraInput.jpg");

		Mat imgHSV = new Mat();

		Mat[] robotMats = new Mat[2];

		Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV);

		// we have two parts of the robot we want to find
		Mat imgThresholded = new Mat();
		Mat imgThresholded2 = new Mat();
		robotMats[0] = imgThresholded;
		robotMats[1] = imgThresholded2;

		Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(
				iHighH, iHighS, iHighV), imgThresholded);

		Core.inRange(imgHSV, new Scalar(iLowH2, iLowS2, iLowV2), new Scalar(
				iHighH2, iHighS2, iHighV2), imgThresholded2);

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
				Core.circle(image, new Point(posX, posY),
						(int) Math.sqrt(dArea / 3.14),
						new Scalar(255, 255, 255));
				// add the robot objects to the ArrayList for pathfinding
				if (j == 0) {
					robot.setBackCord(new Coordinate(posX, posY));
				} else {
					robot.setFrontCord(new Coordinate(posX, posY));
				}
			}
		}

		robot.updateMiddleCord();
		// assemble the 2 robot findings into 1 Mat.
		// convert this Mat to an Image
		robotMats[0].copyTo(robotMats[1], robotMats[0]);
		// convert to buffered image to show on the screen

		outImg = toBufferedImage(robotMats[1]);
	}

	private void findBallsInImage(Mat imageBlurr) {
		/*
		 * Find the circles in the image
		 */
		Mat circles = new Mat();
		Imgproc.HoughCircles(imageBlurr, circles, Imgproc.CV_HOUGH_GRADIENT,
				1.8, 50, 80, 22, 5, 8);

		if (!circles.empty()) {

			for (int i = 0; i <= circles.cols(); i++) {

				double[] coordinate = circles.get(0, i);
				if (coordinate == null) {
					break;
				}

				balls.add(new Ball(coordinate[0], coordinate[1]));
				balls.get(balls.size() - 1).setRadius(coordinate[2]);
			}
		}
	}

	private void drawBalls() {
		for (Ball ball : balls) {
			Core.circle(image, new Point(ball.getX(), ball.getY()),
					(int) ball.getRadius(), new Scalar(0, 0, 0));
		}
	}

	private void identifyLines() {
		/*
		 * Find the lines representing the edge of the field
		 */

		Mat src = Highgui.imread("cameraInput.jpg", 0);
		Mat dst = new Mat();
		Imgproc.cvtColor(src, dst, Imgproc.COLOR_GRAY2BGR);
		Highgui.imwrite("wtf2.jpg", dst);
		Imgproc.Canny(dst, dst, 50, 200, 3, false);

		Mat lines = new Mat();

		Imgproc.HoughLinesP(dst, lines, 1, Math.PI / 180, 50, 360, 350);

		for (int x = 0; x < lines.cols(); x++) {
			double[] vec = lines.get(0, x);
			double x1 = vec[0], y1 = vec[1], x2 = vec[2], y2 = vec[3];
			Point start = new Point(x1, y1);
			Point end = new Point(x2, y2);
			lineCoordinates.add(start);
			lineCoordinates.add(end);

		}
		try {
			drawApproxLines();
		} catch (Exception e) {
			System.out.println("no lines");
		}
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

	private void drawApproxLines() {

		// the robot middle coordinates are already updated
		double midX = robot.getMiddleCord().getX();
		double midY = robot.getMiddleCord().getY();

		Rect topLeftRect = new Rect(new Point(0, 0), new Point(100, 100));
		Rect topRightRect = new Rect(new Point(540, 0), new Point(640, 100));
		Rect bottomLeftRect = new Rect(new Point(0, 380), new Point(100, 480));
		Rect bottomRightRect = new Rect(new Point(540, 380),
				new Point(640, 480));
		Rect robotRectangle = new Rect((int) midX - 20, (int) midY - 20, 40, 40);
		Rect arena = new Rect(new Point(100, 100), new Point(540, 380));

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

		for (int i = 0; i < lineCoordinates.size(); i++) {
			if (lineCoordinates.get(i).inside(arena)
					&& !lineCoordinates.get(i).inside(robotRectangle)) {
				count++;
				x += lineCoordinates.get(i).x;
				y += lineCoordinates.get(i).y;
			}
		}
		x /= count;
		y /= count;

		// Add the cross object and set all the points
		cross = new MiddleCross(x, y);
		cross.setLeftCross(new Coordinate(cross.getCenterOfCross().getX()
				- ((frames.topRight().getX() - frames.topLeft().getX()) / 18),
				cross.getCenterOfCross().getY()));

		cross.setRightCross(new Coordinate(cross.getCenterOfCross().getX()
				+ ((frames.topRight().getX() - frames.topLeft().getX()) / 18),
				cross.getCenterOfCross().getY()));

		cross.setTopCross(new Coordinate(cross.getCenterOfCross().getX(), cross
				.getCenterOfCross().getY()
				- ((frames.lowRight().getY() - frames.topRight().getY()) / 12)));

		cross.setBottomCross(new Coordinate(
				cross.getCenterOfCross().getX(),
				cross.getCenterOfCross().getY()
						+ ((frames.lowRight().getY() - frames.topRight().getY()) / 12)));

		// add the corners to the list of objects that we have to take into
		// consideration
		frames.setTopLeft(new Coordinate(lineTopLeft.x, lineTopLeft.y));
		frames.setTopRight(new Coordinate(lineTopRight.x, lineTopRight.y));
		frames.setLowRight(new Coordinate(lineBottomRight.x, lineBottomRight.y));
		frames.setLowLeft(new Coordinate(lineBottomLeft.x, lineBottomLeft.y));

		int lenghtOfRobot = (int) Math.ceil(Math.sqrt(Math.pow((robot
				.getFrontCord().getX() - robot.getBackCord().getX()), 2)
				+ Math.pow((robot.getFrontCord().getY() - robot.getBackCord()
						.getY()), 2)));

		// add Goal points to the list of objects (where the robot )
		goalA = new Goal(lineBottomRight.x + lenghtOfRobot,
				lineBottomRight.y / 2);
		goalB = new Goal(lineBottomRight.x - lenghtOfRobot,
				lineBottomRight.y / 2);

		// Draw cross
		Core.line(image, new Point(cross.getLeftCross().getX(), cross
				.getLeftCross().getY()), new Point(
				cross.getRightCross().getX(), cross.getRightCross().getY()),
				new Scalar(0, 0, 255), 3);
		Core.line(image, new Point(cross.getTopCross().getX(), cross
				.getTopCross().getY()), new Point(
				cross.getBottomCross().getX(), cross.getBottomCross().getY()),
				new Scalar(0, 0, 255), 3);

		// draw lines
		Core.line(image, lineTopLeft, lineTopRight, new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopLeft, lineBottomLeft, new Scalar(0, 0, 255), 3);
		Core.line(image, lineBottomLeft, lineBottomRight,
				new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopRight, lineBottomRight, new Scalar(0, 0, 255),
				3);

	}

	private void drawVectors() {
		int ballIndex = findClosestBall(balls, robot);

		try {
			Core.line(image, new Point(robot.getBackCord().getX(), robot
					.getBackCord().getY()), new Point(robot.getFrontCord()
					.getX(), robot.getFrontCord().getY()), new Scalar(0, 0, 0));

			Core.line(image, new Point(robot.getBackCord().getX(), robot
					.getBackCord().getY()), new Point(balls.get(ballIndex)
					.getX(), balls.get(ballIndex).getY()), new Scalar(0, 0, 0));
		} catch (Exception e) {
			return;
		}

	}

	private int findClosestBall(ArrayList<Ball> balls, Robot robot) {
		// init with improbable values
		int indexOfClosestBall = -1;
		int shortestDistance = 9999;

		// look through the rest of the array and see if another ball is closer
		for (int i = 0; i < balls.size(); i++) {
			int distance = (int) calcDifference(robot.getFrontCord().getX(),
					robot.getFrontCord().getY(), balls.get(i).getX(), balls
							.get(i).getY());
			if (distance < shortestDistance) {
				indexOfClosestBall = i;
				shortestDistance = distance;
			}
		}
		return indexOfClosestBall;
	}

	int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotFront")) {
				return i;
			}

		}
		return -1;
	}

	private double calcDifference(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
	}

	int findBack(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotBack")) {
				return i;
			}

		}
		return -1;
	}

	/**
	 * Method to remove balls that appears inside robot
	 */
	private void ignoreBallInsideRobot() {

		// we need the back and front of robot to get area to delete from
		double midX = (robot.getBackCord().getX() + robot.getFrontCord().getX()) / 2;
		double midY = (robot.getBackCord().getY() + robot.getFrontCord().getY()) / 2;

		Rectangle robotRectangle = new Rectangle((int) midX - 20,
				(int) midY - 20, 40, 40);

		// run through the list of objects to find balls
		for (Ball ball : balls) {
			// if x and y is between front and back of robot we remove the ball
			if (robotRectangle.contains(ball.getX(), ball.getY())) {
				balls.remove(ball);

			}
		}
	}
}
