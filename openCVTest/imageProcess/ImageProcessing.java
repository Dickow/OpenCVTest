package imageProcess;

import imageCapture.ImageObject;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ImageProcessing implements Runnable {
	private PathFinding2 pathfinder = new PathFinding2();
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

	public ArrayList<NodeObjects> objects;
	public ArrayList<Point> lineCoordinates;
	private Mat image;

	public void run() {

		// Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		Mat frame = new Mat();

		while (true) {
			lineCoordinates = new ArrayList<Point>();
			objects = new ArrayList<NodeObjects>();

			frame = ImageObject.getInstance().getImg();

			Highgui.imwrite("cameraInput.jpg", frame);

			// Consider the image for processing Imgproc.COLOR_BGR2GRAY
			image = Highgui.imread("cameraInput.jpg");
			// image = frame;
			Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
			Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
			// Mat imageA = new Mat(image.size(), Core.DEPTH_MASK_ALL);
			try {
				Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
			} catch (Exception e) {
				System.out.println("frame was empty returning");
				continue;
			}
			Highgui.imwrite("gray.jpg", imageHSV);
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

			if (objects.size() > 0) {
				// remove all illegal balls
				pathfinder.findPath(objects);
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
					objects.add(new NodeObjects(posX, posY, "robotBack"));
				} else {
					objects.add(new NodeObjects(posX, posY, "robotFront"));
				}
			}
		}
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
			// int radius;
			// Point pt;

			for (int i = 0; i <= circles.cols(); i++) {

				double[] coordinate = circles.get(0, i);
				if (coordinate == null) {
					break;
				}
				// pt = new Point(Math.round(coordinate[0]),
				// Math.round(coordinate[1]));
				// radius = (int) Math.round(coordinate[2]);

				// Core.circle(image, pt, radius, new Scalar(0, 0, 0));
				objects.add(new NodeObjects(Math.round(coordinate[0]), Math
						.round(coordinate[1]), "ball"));
			}
		}
	}

	private void drawBalls() {
		for (NodeObjects node : objects) {
			if (node.getType().equalsIgnoreCase("ball")) {
				Core.circle(image, new Point(node.getX(), node.getY()), 8,
						new Scalar(0, 0, 0));
			}
		}
	}

	private void identifyLines() {
		/*
		 * Find the lines representing the edge of the field
		 */

		Mat src = Highgui.imread("cameraInput.jpg", 0);
		Mat dst = new Mat();
		// Imgproc.cvtColor(src, dst, Imgproc.COLOR_YUV420sp2RGB);
		// Highgui.imwrite("wtf1.jpg",dst);
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
			// Core.line(image, start, end, new Scalar(255,0,0), 3);

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
		int backIndex = findBack(objects);
		int frontIndex = findFront(objects);

		double midX = (objects.get(backIndex).getX() + objects.get(frontIndex)
				.getX()) / 2;
		double midY = (objects.get(backIndex).getY() + objects.get(frontIndex)
				.getY()) / 2;

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
		Point cross = new Point(x, y);

		// add the corners to the list of objects that we have to take into
		// consideration
		objects.add(new NodeObjects(lineTopLeft.x, lineTopLeft.y, "LinePoint"));
		objects.add(new NodeObjects(lineTopRight.x, lineTopRight.y, "LinePoint"));
		objects.add(new NodeObjects(lineBottomLeft.x, lineBottomLeft.y,
				"LinePoint"));
		objects.add(new NodeObjects(lineBottomRight.x, lineBottomRight.y,
				"LinePoint"));

		// Add crss to the list of objects
		objects.add(new NodeObjects(cross.x, cross.y, "cross"));

		// add Goals to the list of objects
		objects.add(new NodeObjects(lineBottomRight.x, Math
				.round(lineBottomRight.y / 2), "GoalA"));
		objects.add(new NodeObjects(lineBottomRight.x, Math
				.round(lineBottomRight.y / 2), "GoalB"));

		// Draw cross
		Core.line(image, cross, cross, new Scalar(0, 0, 255), 3);

		// draw lines
		Core.line(image, lineTopLeft, lineTopRight, new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopLeft, lineBottomLeft, new Scalar(0, 0, 255), 3);
		Core.line(image, lineBottomLeft, lineBottomRight,
				new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopRight, lineBottomRight, new Scalar(0, 0, 255),
				3);

	}

	private void drawVectors() {
		int ballIndex = findClosestBall(objects);
		int backIndex = findBack(objects);
		int frontIndex = findFront(objects);
		try {
			Core.line(image, new Point(objects.get(backIndex).getX(), objects
					.get(backIndex).getY()), new Point(objects.get(frontIndex)
					.getX(), objects.get(frontIndex).getY()), new Scalar(0, 0,
					0));

			Core.line(image, new Point(objects.get(backIndex).getX(), objects
					.get(backIndex).getY()), new Point(objects.get(ballIndex)
					.getX(), objects.get(ballIndex).getY()),
					new Scalar(0, 0, 0));
		} catch (Exception e) {
			return;
		}

	}

	/**
	 * @param objects
	 * @return
	 */
	private int findClosestBall(ArrayList<NodeObjects> objects) {
		double tmpLength;
		double min_length = 1000;
		int min_index = -1;
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("ball")) {
				if ((tmpLength = calcLength(objects.get(i),
						objects.get(findFront(objects)))) < min_length) {
					min_length = tmpLength;
					min_index = i;
				}
			}
		}

		/* remove this when done testing */
		try {
			System.out.println(objects.get(min_index).toString());
		} catch (ArrayIndexOutOfBoundsException e) {
			return -1;
		}
		return min_index;
	}

	int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("robotFront")) {
				return i;
			}

		}
		return -1;
	}

	private double calcLength(NodeObjects first, NodeObjects second) {
		return Math.sqrt(Math.pow((first.getX() - second.getX()), 2)
				+ Math.pow((first.getY() - second.getY()), 2));
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
		int backIndex = findBack(objects);
		int frontIndex = findFront(objects);

		double midX = (objects.get(backIndex).getX() + objects.get(frontIndex)
				.getX()) / 2;
		double midY = (objects.get(backIndex).getY() + objects.get(frontIndex)
				.getY()) / 2;

		Rectangle robotRectangle = new Rectangle((int) midX - 20,
				(int) midY - 20, 40, 40);

		// run through the list of objects to find balls
		for (int i = 0; i < objects.size(); i++) {

			// if x and y is between front and back of robot we remove the ball
			if (objects.get(i).getType().equals("ball")
					&& robotRectangle.contains(objects.get(i).getX(), objects
							.get(i).getY())) {

				objects.remove(i);
				i--;
			}
		}
	}
}
