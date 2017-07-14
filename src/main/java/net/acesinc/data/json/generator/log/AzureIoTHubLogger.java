/**
 * 
 */
package net.acesinc.data.json.generator.log;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.Message;

/**
 * @author jurgenma
 *
 */
public final class AzureIoTHubLogger implements EventLogger {

	private final DeviceClient deviceClient;
	
	/**
	 * @param props
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public AzureIoTHubLogger(final Map<String, Object> props) throws URISyntaxException, IOException {
		
		super();
		
		this.deviceClient = new DeviceClient((String)props.get("connectionString"), 
				IotHubClientProtocol.valueOf((String)props.get("protocol")));
		this.deviceClient.open();
	}
	
	/**
	 * @param deviceClient
	 */
	public AzureIoTHubLogger(final DeviceClient deviceClient) {
		super();
		this.deviceClient = deviceClient;
	}

	/* (non-Javadoc)
	 * @see net.acesinc.data.json.generator.log.EventLogger#logEvent(java.lang.String, java.util.Map)
	 */
	@Override
	public void logEvent(String event, Map<String, Object> producerConfig) {
		
		this.deviceClient.sendEventAsync(new Message(event), null, null);
	}

	/* (non-Javadoc)
	 * @see net.acesinc.data.json.generator.log.EventLogger#shutdown()
	 */
	@Override
	public void shutdown() {
		
		if (this.deviceClient != null) {
			try {
				this.deviceClient.closeNow();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
