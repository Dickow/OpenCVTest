package imageProcess;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.sun.javafx.geom.Vec2f;

public class ContourTest implements Runnable {
	//private Pathfinding pathfinder = new Pathfinding();
	public int ballSize = 5;
	public int iLowH = 29;
	public int iHighH = 62;

	public int iLowS = 41;
	public int iHighS = 145;

	public int iLowV = 112;
	public int iHighV = 200;

	public Image outImg, outImg2;

	public ArrayList<NodeObjects> objects;
	public ArrayList<Point> lineCoordinates;
	private Mat image;

	public void run() {

		// Load the library
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		// get a picture from the webcam and save it VideoCapture
		VideoCapture videoCapture = new VideoCapture(1);
		if (!videoCapture.isOpened()) {
			System.out.println("could not find video ");
		} else {
			System.out.println(" webcam was found: " + videoCapture.toString());
		}
		Mat frame = new Mat();

		while (true) {
			lineCoordinates = new ArrayList<Point>();
			objects = new ArrayList<NodeObjects>();

			videoCapture.read(frame);
			Highgui.imwrite("cameraInput.jpg", frame);

			// Consider the image for processing Imgproc.COLOR_BGR2GRAY
			image = Highgui.imread("cameraInput.jpg");
			Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
			Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
			// Mat imageA = new Mat(image.size(), Core.DEPTH_MASK_ALL);
			Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
			Highgui.imwrite("gray.jpg", imageHSV);
			Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(1, 1), 2, 2);

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
			drawApproxLines();

			/*
			 * Find the circles in the image
			 */
			Mat circles = new Mat();
			Imgproc.HoughCircles(imageBlurr, circles,
					Imgproc.CV_HOUGH_GRADIENT, 1, 10, 200, 10, 0, 10);

			if (!circles.empty()) {
				int radius;
				Point pt;

				for (int i = 0; i <= circles.cols(); i++) {

					double[] coordinate = circles.get(0, i);
					if (coordinate == null) {
						break;
					}
					pt = new Point(Math.round(coordinate[0]),
							Math.round(coordinate[1]));
					radius = (int) Math.round(coordinate[2]);

					Core.circle(image, pt, radius, new Scalar(0, 0, 0));
					objects.add(new NodeObjects(Math.round(coordinate[0]), Math
							.round(coordinate[1]), "ball"));
				}
			}
			
			// add the robot for testing
//			objects.add(new NodeObjects(500, 300, "FrontRobot"));
//			objects.add(new NodeObjects(500, 340, "BackRobot"));
//			Core.circle(image, new Point(500, 300),
//					(int) Math.sqrt(49),
//					new Scalar(255, 255, 255));
//			Core.circle(image, new Point(500, 340),
//					(int) Math.sqrt(49),
//					new Scalar(255, 255, 255));
//			
//			// draw a test triangle delete when done TODO
//			Core.line(image, new Point(500,300), new Point(190, 49), new Scalar(255,255,0));
//			Core.line(image, new Point(500,300), new Point(500, 320), new Scalar(255,255,0));
//			Core.line(image, new Point(500,320), new Point(190, 49), new Scalar(255,255,0));
			
			// find the robot with color scan

			Mat imgOriginal = Highgui.imread("cameraInput.jpg");

			Mat imgHSV = new Mat();

			Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV);

			Mat imgThresholded = new Mat();

			Core.inRange(imgHSV, new Scalar(iLowH, iLowS, iLowV), new Scalar(
					iHighH, iHighS, iHighV), imgThresholded);

			// morphological opening (removes small objects from the foreground)
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));

			// morphological closing (removes small holes from the foreground)
			Imgproc.erode(imgThresholded, imgThresholded, Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));
			Imgproc.dilate(imgThresholded, imgThresholded, Imgproc
					.getStructuringElement(Imgproc.MORPH_ELLIPSE,
							new Size(5, 5)));

			Moments oMoments = Imgproc.moments(imgThresholded, true);

			double dM01 = oMoments.get_m01();
			double dM10 = oMoments.get_m10();
			double dArea = oMoments.get_m00();

			// if the area <= 10000, I consider that the there are no object in
			// the image and it's because of the noise, the area is not zero
			if (dArea > 100) {
				// calculate the position of the ball
				double posX = dM10 / dArea;
				double posY = dM01 / dArea;
				// System.out.println("posX = " + posX);
				// System.out.println("posY = " + posY);
				Core.circle(image, new Point(posX, posY),
						(int) Math.sqrt(dArea / 3.14),
						new Scalar(255, 255, 255));
				objects.add(new NodeObjects(posX, posY, "robotFront"));
			}

			// convert to buffered image to show on the screen
			outImg2 = toBufferedImage(image);
			outImg = toBufferedImage(imgThresholded);

			for (int i = 0; i < objects.size(); i++) {
				System.out.println(objects.get(i).toString());
			}
			System.out.println("*****************************************");
			//pathfinder.run(objects);
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
		Rect topLeftRect = new Rect(new Point(0, 0), new Point(100, 100));
		Rect topRightRect = new Rect(new Point(540, 0), new Point(640, 100));
		Rect bottomLeftRect = new Rect(new Point(0, 380), new Point(100, 480));
		Rect bottomRightRect = new Rect(new Point(540, 380),
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
		
		// add the corners to the list of objects that we have to take into consideration
		objects.add(new NodeObjects(lineTopLeft.x, lineTopLeft.y, "LinePoint"));
		objects.add(new NodeObjects(lineTopRight.x, lineTopRight.y, "LinePoint"));
		objects.add(new NodeObjects(lineBottomLeft.x, lineBottomLeft.y, "LinePoint"));
		objects.add(new NodeObjects(lineBottomRight.x, lineBottomRight.y, "LinePoint"));
		
		// draw lines
		Core.line(image, lineTopLeft, lineTopRight, new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopLeft, lineBottomLeft, new Scalar(0, 0, 255), 3);
		Core.line(image, lineBottomLeft, lineBottomRight,
				new Scalar(0, 0, 255), 3);
		Core.line(image, lineTopRight, lineBottomRight, new Scalar(0, 0, 255),
				3);
	}
}
