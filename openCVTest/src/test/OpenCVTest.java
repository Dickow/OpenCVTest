package test;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;

public class OpenCVTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		
		VideoCapture videoCapture = new VideoCapture(0); 
		if(! videoCapture.isOpened()){
			System.out.println("could not find video ");
		}else{
			System.out.println(" webcam was found: " + videoCapture.toString());
			Mat frame = new Mat(); 
			
			videoCapture.read(frame);
			
			Highgui.imwrite("test1.jpg", frame); 
		}
	}

}
