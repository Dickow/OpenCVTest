package test;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

public class ContourTest implements Runnable {
	public int ballSize = 5;
	public int iLowH = 29;
	public int iHighH = 62;

	public int iLowS = 41;
	public int iHighS = 145;

	public int iLowV = 112;
	public int iHighV = 200;

	public Image outImg, outImg2;

	public ArrayList<NodeObjects> objects;

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
			objects = new ArrayList<NodeObjects>();

			videoCapture.read(frame);
			Highgui.imwrite("cameraInput.jpg", frame);
			
			// Consider the image for processing Imgproc.COLOR_BGR2GRAY
			Mat image = Highgui.imread("cameraInput.jpg");
			Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
			Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
			Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
			Highgui.imwrite("gray.jpg", imageHSV); 
			Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(1, 1), 2, 2);
			
			
			Highgui.imwrite("blurred.jpg", imageBlurr); 

			Mat circles = new Mat();
			Imgproc.HoughCircles(imageBlurr, circles,
					Imgproc.CV_HOUGH_GRADIENT, 1, 10, 200,10, 0, 10);
			

			if (!circles.empty()) {
				int radius;
				Point pt;

				System.out.println("found circle");
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
			System.out.println("the image is set");
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
}
// }
