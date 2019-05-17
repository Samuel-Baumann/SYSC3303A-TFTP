import java.net.InetAddress;

/**
 * @author Group 5
 * @version 5/21/2018 (Iteration #1)
 * 
 * Print helper class
 */
public class Print {
	private Constants.ModeType mode;

	/**
	 * 
	 * @param mode
	 */
	public Print(Constants.ModeType mode) {
		this.mode = mode;
	}

	/**
	 * 
	 * @param serverSender
	 * @param serverReciever
	 * @param address
	 * @param port
	 * @param length
	 * @param data
	 */
	public void PrintSendingPackets(Constants.ServerType serverSender, Constants.ServerType serverReciever, InetAddress address, int port, int length, byte[] data) {
		if (mode != Constants.ModeType.QUIET) {
			System.out.println("[" + serverSender.getServerName() + "]: Sending packet.");
			System.out.println("To " + serverReciever.getServerName() + ": " + address);
			System.out.println("Destination " + serverReciever.getServerName() + " port: " + port);
			System.out.println("Length: " + length);
			System.out.println("Packet Type: " + getPacketType(data));
			System.out.println("Containing (String): " + new String(data,0,length));
			String byteReceivedData = "|";
			for(int i = 0; i < length; i++) {
				byteReceivedData += data[i] + "|";
			}
			System.out.println("Containing (byte): " + byteReceivedData + "\n");
		}
	}

	/**
	 * 
	 * @param serverReciever
	 * @param serverSender
	 * @param address
	 * @param port
	 * @param length
	 * @param data
	 */
	public void PrintReceivedPackets(Constants.ServerType serverReciever, Constants.ServerType serverSender, InetAddress address, int port, int length, byte[] data) {
		if (mode != Constants.ModeType.QUIET) {
			System.out.println("[" + serverReciever.getServerName() + "]: Packet recieved");
			System.out.println("From " + serverSender.getServerName() + ": " + address);
			System.out.println(serverSender.getServerName() + " port: " + port);
			System.out.println("Length: " + length);
			System.out.println("Packet Type: " + getPacketType(data));
			System.out.println("Containing (String): " + new String(data,0,length));
			String byteReceivedData = "|";
			for(int i = 0; i < length; i++) {
				byteReceivedData += data[i] + "|";
			}
			System.out.println("Containing (byte): " + byteReceivedData + "\n");
		}
	}

	/**
	 * 
	 * @param data
	 * @return
	 */
	private String getPacketType(byte[] data) {
		String packetType = "";
		if (data[0] != 0) {
			packetType = Constants.PacketString.ERROR.getPacketStringType();
		} else if (data[1] == 1) {
			packetType = Constants.PacketString.RRQ.getPacketStringType();
		} else if (data[1] == 2) {
			packetType = Constants.PacketString.WRQ.getPacketStringType();
		} else if (data[1] == 3) {
			packetType = Constants.PacketString.DATA.getPacketStringType();
		} else if (data[1] == 4) {
			packetType = Constants.PacketString.ACK.getPacketStringType();
		} else {
			packetType = Constants.PacketString.ERROR.getPacketStringType();
		}
		return packetType;
	}
}