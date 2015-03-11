package test;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
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
import org.opencv.ml.CvStatModel;

public class ContourTest {
	private static int ballSize = 30;

	public static void main(String[] args) {

		// Load the library

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		/*
		 * // get a picture from the webcam and save it VideoCapture
		 * videoCapture = new VideoCapture(1); if (!videoCapture.isOpened()) {
		 * System.out.println("could not find video "); } else {
		 * System.out.println(" webcam was found: " + videoCapture.toString());
		 * Mat frame = new Mat();
		 * 
		 * videoCapture.read(frame); videoCapture.read(frame);
		 * Highgui.imwrite("test1.jpg", frame);
		 */

		// Consider the image for processing
		Mat image = Highgui.imread("robotFind.jpg", Imgproc.COLOR_BGR2GRAY);
		Mat imageHSV = new Mat(image.size(), Core.DEPTH_MASK_8U);
		Mat imageBlurr = new Mat(image.size(), Core.DEPTH_MASK_8U);
		Mat imageA = new Mat(image.size(), Core.DEPTH_MASK_ALL);
		Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5, 5), 0);
		Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,
				Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 7, 5);
		Highgui.imwrite("contoursOut1.jpg", imageBlurr);
		Highgui.imwrite("contoursOut3.jpg", imageA);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_TREE,
				Imgproc.CHAIN_APPROX_SIMPLE);

		for (int i = 0; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > 50) {
				Rect rect = Imgproc.boundingRect(contours.get(i));
				System.out.println(rect.area());
				if (rect.height > ballSize && rect.width > ballSize
						&& rect.height < 100 && rect.width < 100
						|| rect.height > 30 && rect.width > 30
						&& rect.height < 50 && rect.width < 50) {
					Core.rectangle(image, new Point(rect.x, rect.y), new Point(
							rect.x + rect.width, rect.y + rect.height),
							new Scalar(0, 0, 255));
					System.out.println("rect x : " + rect.x + " rect y : "
							+ rect.y);
				}
			}
		}

		// find the robot with color scan
		int iLowH = 145;
		int iHighH = 180;

		int iLowS = 0;
		int iHighS = 255;

		int iLowV = 0;
		int iHighV = 255;

		Highgui.imwrite("contoursOut2.jpg", image);

		int lastX = -1;
		int lastY = -1;

		Mat imgTmp = Highgui.imread("robotFind.jpg");
		Mat imgLines = new Mat(imgTmp.size(), CvType.CV_8UC3);
		Mat imgOriginal = Highgui.imread("robotFind.jpg");

		Mat imgHSV = new Mat();

		Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV);
		
		Mat imgThresholded = new Mat(); 
		
		Core.inRange(imgHSV,new Scalar(iLowH,iLowS,iLowV), new Scalar(iHighH,iHighS,iHighV), imgThresholded);
		
		//morphological opening (removes small objects from the foreground)
		Imgproc.erode(imgThresholded, imgThresholded,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		Imgproc.dilate(imgThresholded, imgThresholded,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		
		//morphological closing (removes small holes from the foreground)
		Imgproc.erode(imgThresholded, imgThresholded,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		Imgproc.dilate(imgThresholded, imgThresholded,Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		
		Moments oMoments = Imgproc.moments(imgThresholded, true);
		
		double dM01 = oMoments.get_m01();
		double dM10 = oMoments.get_m10();
		double dArea = oMoments.get_m00();
		
		 // if the area <= 10000, I consider that the there are no object in the image and it's because of the noise, the area is not zero 
		if (dArea > 10000)
		{
		//calculate the position of the ball
		double posX = dM10 / dArea;
		double posY = dM01 / dArea;
		 System.out.println("posX = " + posX);
		 System.out.println("posY = " + posY);
		}
		Highgui.imwrite("imgThresholded.jpg", imgThresholded); 

	}
}
// }
