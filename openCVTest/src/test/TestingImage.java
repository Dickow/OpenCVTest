package test;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class TestingImage {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		
		Mat testingFrame = new Mat(); 
		
		VideoCapture videoCapture = new VideoCapture(1); 
		
		if(!videoCapture.isOpened()){
			System.out.println("could not finde camera");
		}
		
		videoCapture.read(testingFrame); 
		videoCapture.read(testingFrame); 
		
		Highgui.imwrite("robot.jpg", testingFrame); 

	}

}
