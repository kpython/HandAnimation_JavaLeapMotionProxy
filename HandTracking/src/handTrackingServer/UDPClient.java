/**
 * Hand Animation on iOS
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 * This class is UDP client used to send information to the iOS App.
 */

package handTrackingServer;

import java.net.*;

public class UDPClient {

	DatagramSocket ds;
	DatagramPacket dp;

	public UDPClient(int localport) throws SocketException {
		this.ds = new DatagramSocket(localport);
	}

	public void sendMsg(InetSocketAddress isaServer, String msg)
			throws Exception {
		
		dp = new DatagramPacket(
				msg.getBytes(),
				msg.length(),
				isaServer.getAddress(),
				isaServer.getPort());

		ds.send(dp);
	}

	public void closeSocket() {
		ds.close();
	}

	public int getPort() {
		return ds.getLocalPort();
	}
}