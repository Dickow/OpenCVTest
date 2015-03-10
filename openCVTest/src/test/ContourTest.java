package test;

import java.util.ArrayList;
import java.util.List;

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

public class ContourTest {

	public static void main(String[] args) {
		// Load the library

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
/*
		// get a picture from the webcam and save it
		VideoCapture videoCapture = new VideoCapture(1);
		if (!videoCapture.isOpened()) {
			System.out.println("could not find video ");
		} else {
			System.out.println(" webcam was found: " + videoCapture.toString());
			Mat frame = new Mat();

			videoCapture.read(frame);
			videoCapture.read(frame);
			Highgui.imwrite("test1.jpg", frame);
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
			Imgproc.findContours(imageA, contours, new Mat(),
					Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

			for (int i = 0; i < contours.size(); i++) {
				if (Imgproc.contourArea(contours.get(i)) > 50) {
					Rect rect = Imgproc.boundingRect(contours.get(i));
					System.out.println(rect.area());
					if (rect.height > 50 && rect.width > 50
							&& rect.height < 200 && rect.width < 200
							|| rect.height > 30 && rect.width > 30
							&& rect.height < 50 && rect.width < 50) {
						Core.rectangle(image, new Point(rect.x, rect.y),
								new Point(rect.x + rect.width, rect.y
										+ rect.height), new Scalar(0, 0, 255));
						System.out.println("rect x : " + rect.x + " rect y : "
								+ rect.y);
					}
				}
			}
			Highgui.imwrite("contoursOut2.jpg", image);
		}
	}
//}
