/**
 * Hand Animation on iOS
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 * This class manage incoming TCP connections by creating TCPSocketThread and start them.
 * 
 */

package handTrackingServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPServerThread extends Thread {

	ServerSocket serverSocket;
	LeapListener listener;
	int id;

	public TCPServerThread(LeapListener listener, ServerSocket serverSocket) {
		this.listener = listener;
		this.serverSocket = serverSocket;
		this.id = 0;
	}

	public void run() {
		Socket socket;
		try {
			// Start server after Bonjour registration
			Thread.sleep(1000);
			System.out.println("Server Thread started, wait for incoming connections...");
			while (true){
				socket = serverSocket.accept();
				TCPSocketThread t = new TCPSocketThread(socket, listener,id++);
				t.start();
	    	}
		} catch (SocketException e) {
			// This catch clause is reached when program is manually ended
			System.out.println("Server socket has been closed");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
