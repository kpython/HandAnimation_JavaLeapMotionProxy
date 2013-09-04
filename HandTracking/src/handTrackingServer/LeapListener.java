/**
 * Hand Animation on iOS
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 * This class is responsible to listen Leap Motion controller events. Each time a new frame is received,
 * hand location, rotation, finger flexion are computed. These information are then grouped in a JSON file 
 * to be easily transmitted.
 */

package handTrackingServer;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.leapmotion.leap.*;

class LeapListener extends Listener {
	
	public	String handTrackingFrame;
	
	private Finger[] fingers;
	private float[] fingerFlexion;
	private boolean calibrationNeeded;

	
	public void onInit(Controller controller) {
		System.out.println("Leap controller initialized");
		fingers = new Finger[5];
		fingerFlexion = new float[5];
		calibrationNeeded = true;
	}

	public void onConnect(Controller controller) {
		System.out.println("Leap controller connected");
		/*
		 * System.out.println("range: "+leap.range());
		 * System.out.println("horizontal view angle" +
		 * leap.horizontalViewAngle()); System.out.println("vertical view angle"
		 * + leap.verticalViewAngle());
		 */
	}

	public void onDisconnect(Controller controller) {
		System.out.println("Leap controller disconnected");
	}

	public void onExit(Controller controller) {
		System.out.println("Leap controller exited");
	}

	public void onFrame(Controller controller) {
		// Get the most recent frame
		Frame frame = controller.frame();
		
		// Compute hand characteristic from the received frame
		handTrackingFrame = computeHandCharacteristics(frame);
	}

	/* This method will compute a value between 0 and 1 expressing the flexion of the finger.
	* This value is computed using the angle between the finger direction
	* vector and the normal of the palm.
	*/
	public float computeFingerFlexion(Vector fingerDirection, Vector normal){
    	float angleToNormal = fingerDirection.angleTo(normal);
    	
    	// Restrict this value to be between pi/2 and 0
    	if (angleToNormal >= Math.PI/2){
    		return 0.0f;
    	}
    	else if (angleToNormal <= 0){
    		return 1.0f;
    	}

    	// Reverse this value to express the finger flexion
    	// 	0.0 == no flexion (finger straight)
    	//  PI/2 == finger is fully flexed 
    	float fingerFlexion = (float) (Math.PI/2 - angleToNormal);
    	
    	// Scale this value between 0.0 and 1.0
    	return fingerFlexion /= (Math.PI/2 - 0.2); 
    }
	
	public String computeHandCharacteristics(Frame frame){
		if (!frame.hands().isEmpty()) {
			// Get the first hand
			Hand hand = frame.hands().get(0);

			// Get the hand's normal vector and direction
			// Vector normal = hand.palmNormal();
			Vector direction = hand.direction();
			Vector normal = hand.palmNormal();

			// Get the hand's position
			float xPosition = hand.palmPosition().getX();
			float yPosition = hand.palmPosition().getY();
			float zPosition = hand.palmPosition().getZ();

			// Calculate the hand's pitch, roll, and yaw angles
			float pitch = (float) Math.toDegrees(direction.pitch()); // x-axis
			float yaw = (float) Math.toDegrees(direction.yaw()); // y-axis
			float roll = (float) Math.toDegrees(normal.roll()); // z-axis

			// Retrieve each fingers and calculate angle from hand direction
			FingerList fingerList = hand.fingers();

			// if all fingers are detected, calibrate the system
            if (fingerList.count() == 5 && calibrationNeeded){
            	int i = 0;
            	// Save every fingers in a array
                for (Finger finger : fingerList){
                	fingers[i++] = finger; 	
                	//System.out.println(finger);
                }
                // Sort fingers according their position on the x axis
                Arrays.sort(fingers, new FingerPositionComparator());  
                if (!(normal.roll() > -(Math.PI/2) && normal.roll() < Math.PI/2)){
                	// Palm is up
                	Collections.reverse(Arrays.asList(fingers));
                }
                calibrationNeeded = false;
            }
            
            
            // Retrieve fingers directions in the new frame
            for(int j=0; j<5; j++){
            	// Get direction of the finger
            	if (fingers[j] != null){
            		Vector fingerDirection = hand.pointable(fingers[j].id()).direction();
                	if (!(fingerDirection.getX() == 0 && fingerDirection.getY() == 0 && fingerDirection.getZ() == 0)){
                		fingerFlexion[j] = computeFingerFlexion(fingerDirection, normal);
                	}
            	}
            }
                       
            // Calibration is needed as soon as the tracking of some fingers is lost
            if (fingerList.count() < 5){
            	calibrationNeeded = true;
            }
            
            // If no finger are detected, the hand is probably closed
            if (fingerList.count() == 0){
            	for(int j=0; j<5; j++){
            		fingerFlexion[j] = 1.0f;
                }
            }

			// Create a JSON Object and truncate float value to delete
			// insignificant data.
			DecimalFormat df = new DecimalFormat("#.##");
			try {
				JSONObject rootObject = new JSONObject();
				rootObject.put("FrameID", frame.id());
				// The frame.timestamp() is expressed in microseconds.
				// This accuracy is not necessary that is why it will be converted in milliseconds
				rootObject.put("timestamp", frame.timestamp()/1000);

				JSONArray palmRotationArray = new JSONArray();
				palmRotationArray.put(df.format(pitch));
				palmRotationArray.put(df.format(yaw));
				palmRotationArray.put(df.format(roll));

				JSONArray palmPositionArray = new JSONArray();
				palmPositionArray.put(df.format(xPosition));
				palmPositionArray.put(df.format(yPosition));
				palmPositionArray.put(df.format(zPosition));

				JSONArray fingersFlexion = new JSONArray();
				fingersFlexion.put(df.format(fingerFlexion[0]));
				fingersFlexion.put(df.format(fingerFlexion[1]));
				fingersFlexion.put(df.format(fingerFlexion[2]));
				fingersFlexion.put(df.format(fingerFlexion[3]));
				fingersFlexion.put(df.format(fingerFlexion[4]));

				JSONObject handObject = new JSONObject();
				handObject.put("palmRotation", palmRotationArray);
				handObject.put("palmPosition", palmPositionArray);
				handObject.put("fingersFlexion", fingersFlexion);

				JSONArray handArray = new JSONArray();
				handArray.put(handObject);

				rootObject.put("hands", handArray);
				
				return rootObject.toString();
			
			} catch (JSONException jsonException) {
				// print error
				System.err.println(jsonException);
			}
		}
		return "";
	}
}

class FingerPositionComparator implements Comparator<Finger> {
	@Override
	public int compare(Finger finger1, Finger finger2) {
		return (int) (finger1.tipPosition().getX() - finger2.tipPosition()
				.getX());
	}
}
