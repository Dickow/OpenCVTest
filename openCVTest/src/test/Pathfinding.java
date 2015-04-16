package test;

import java.util.ArrayList;



public class Pathfinding {
	
	public void run(ArrayList<NodeObjects> objects) {
		double tmpLength;
		int robotFront = findFront(objects);
		int backRobot = findBack(objects);
		double min_length = 1000;
		int min_index = -1;
		
		for(int i = 0; i < objects.size(); i++){
			if(objects.get(i).getType().equals("ball")){
				if(min_index == -1){
					min_index = i; 
				}else if((tmpLength = calcLength(objects.get(i), objects.get(robotFront)))<min_length){
					min_length = tmpLength;
					min_index = i;
				}
			}
		}

	}

	int findFront(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("FrontRobot")) {
				return i;
			}

		}
		return -1;
	}

	int findBack(ArrayList<NodeObjects> objects) {
		for (int i = 0; i < objects.size(); i++) {
			if (objects.get(i).getType().equals("BackRobot")) {
				return i;
			}

		}
		return -1;
	}
	/**
	 * Calculate the length from second to first
	 * 
	 * @param first
	 * @param second
	 * @return
	 */
	private double calcLength(NodeObjects first, NodeObjects second) {
		return Math.sqrt(Math.pow((first.getX() - second.getX()), 2) + Math.pow((first.getY() - second.getY()), 2));
	}
	
	private double findRotationAngle(NodeObjects robotFront, NodeObjects robotMiddle, NodeObjects ball){
		
		
		return (Double) null;
	}
	
	
}
