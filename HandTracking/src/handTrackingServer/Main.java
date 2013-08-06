/**
 * Hand Animation on iOS
 * The goal of this project is to animate a 3D hand model on iOS using coordinate received
 * from the Leap Motion device. Coordinates received from the Leap Motion device are received
 * on a java program, formated in JSON and transmitted to the iOS app through a TCP socket. 
 * Information sent include:
 *  - Hand position
 *  - Hand rotation
 *  - Fingers flexion
 * 
 * The iOS program receives coordinates and animate the 3d hand model according decoded
 * informations.
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 */

package handTrackingServer;

import java.io.IOException;
import java.net.ServerSocket;

import com.leapmotion.leap.Controller;

public class Main {
	
	public static void main(String[] args) {
		ServerSocket serverSocket;
		BonjourRegister bonjourRegister;
		
		try {
			// Let system allocate us an available port to listen on
			serverSocket = new ServerSocket(0);
			
			// Register the handTrackingSystem service on the allocated port
			bonjourRegister = new BonjourRegister("handTrackingService", "_handTracking._tcp" ,serverSocket.getLocalPort());

			// Create a leap controller and listener
	        Controller controller = new Controller();       	        
	        LeapListener listener = new LeapListener();
	      
	        // Create and start a server thread to manage incoming connections 
	        Thread serverThread = new TCPServerThread(listener, serverSocket);
	        serverThread.start();
	        
	        // assign this listener to respond events from the leap controller
	        controller.addListener(listener);
	   
	        // Keep this program running until Enter is pressed
	        System.out.println("Press Enter to quit...");
	        try {
	            System.in.read();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    	System.out.println("Program exited");
	    	
	    	// Remove the leap listener and close server socket
	    	serverSocket.close();
	    	bonjourRegister.stopRegistration();
	    	
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
