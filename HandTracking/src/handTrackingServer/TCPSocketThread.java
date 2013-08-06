/**
 * Hand Animation on iOS
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 * TCPSocketThread is responsible to periodically check for new frames of data and send it to the client (iOS App). 
 * 
 */

package handTrackingServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class TCPSocketThread extends Thread{
	public static final int UPDATE_TIME = 5;
	public static final int UDP_DESTINATION_PORT = 7777;
	public static final int UDP_LOCAL_PORT = 1201;
	
	Socket socket;
	LeapListener listener;
	UDPClient udpClient;
	InetSocketAddress isaDest;
	int id;

	public TCPSocketThread(Socket so, LeapListener listener, int id) {
		this.socket = so;
		this.listener = listener;
		this.id = id;
	}

	public void run() {
		System.out.println(">> :" + this.id + " New connection request from /" + socket.getInetAddress() + ":" + socket.getPort());
		isaDest = new InetSocketAddress(socket.getInetAddress(), UDP_DESTINATION_PORT);
		try {
			udpClient = new UDPClient(UDP_LOCAL_PORT+id);
			String previousFrame = "";
			String currentFrame = "";
			while (true) {
				currentFrame = listener.handTrackingFrame;
				// Send data frame only if new data are available
				if (currentFrame != null){
					if (!currentFrame.equals("") && !previousFrame.equals(currentFrame)){						
						udpClient.sendMsg(isaDest, currentFrame);					
						System.out.println("id:" + this.id + " " + currentFrame);
						previousFrame = currentFrame;
					}
				}
				Thread.sleep(UPDATE_TIME);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
