/**
 * Hand Animation on iOS
 * 
 * @author Kevin Python
 * @version 1.0
 * @since 27.06.13
 * 
 * This class will register the handTrackingService, so that the iOS app will be able to find the IP address
 * of the station where this program will be executed. This class use the java package provided by apple for
 * the use of Bonjour protocol.
 */

package handTrackingServer;

import com.apple.dnssd.*;

class BonjourRegister implements RegisterListener {
	private String serviceType;
	private String serviceName;
	private int servicePort;
	private DNSSDRegistration registration;

	// Display error message on failure
	public void operationFailed(DNSSDService service, int errorCode) {
		System.out.println("Registration failed " + errorCode);
	}

	// Display registered name on success
	public void serviceRegistered(DNSSDRegistration registration, int flags,
			String serviceName, String regType, String domain) {
		System.out.println("Registered Name  : " + serviceName);
		System.out.println("           Type  : " + regType);
		System.out.println("           Domain: " + domain);
	}

	// Do the registration
	public BonjourRegister(String serviceName, String serviceType, int servicePort) {
		this.serviceName = serviceName;
		this.servicePort = servicePort;
		this.serviceType = serviceType;
		doRegistration();
	}
	
	public void doRegistration(){
		System.out.println("Registration Starting");
		System.out.println("Requested Name: " + serviceName);
		System.out.println("          Port: " + servicePort);
		try {
			this.registration = DNSSD.register(serviceName,serviceType,servicePort, this);
		} catch (DNSSDException e) {
			e.printStackTrace();
		}
	}
	
	public void stopRegistration(){
		System.out.println("Registration Stopping");
		registration.stop();
	}
}