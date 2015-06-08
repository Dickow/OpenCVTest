package imageCapture;

import org.opencv.core.Mat;

public class ImageObject {
	
	// variables
	private static ImageObject instance = null; 
	private Mat matImg = new Mat(); 
	
	protected ImageObject(){
		
	}
	
	public static ImageObject getInstance(){
		if(instance == null){
			instance = new ImageObject();
		}
		return instance; 
	}
	
	
	public synchronized Mat getImg(){
		
		if(matImg != null){
			return this.matImg;
		}else{
			return null; 
		}
	}
	
	public synchronized void setImg(Mat newImg){
		this.matImg = newImg; 
	}

}
